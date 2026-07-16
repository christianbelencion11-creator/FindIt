const crypto = require("crypto");
const admin = require("firebase-admin");
const functions = require("firebase-functions");
const nodemailer = require("nodemailer");

admin.initializeApp();

const OTP_TTL_MS = 10 * 60 * 1000;

function normalizeEmail(email) {
  return String(email || "").trim().toLowerCase();
}

function emailDocId(email) {
  return crypto.createHash("sha256").update(normalizeEmail(email)).digest("hex");
}

function generateOtp() {
  return String(Math.floor(100000 + Math.random() * 900000));
}

function getMailTransport() {
  const cfg = functions.config().otp || {};
  const user = process.env.OTP_EMAIL_USER || cfg.user;
  const pass = process.env.OTP_EMAIL_PASS || cfg.pass;
  if (!user || !pass) {
    throw new functions.https.HttpsError(
      "failed-precondition",
      "Email sender is not configured. Set OTP_EMAIL_USER and OTP_EMAIL_PASS secrets."
    );
  }
  return nodemailer.createTransport({
    service: "gmail",
    auth: { user, pass },
  });
}

async function findUserByEmail(email) {
  try {
    return await admin.auth().getUserByEmail(email);
  } catch (error) {
    if (error.code === "auth/user-not-found") {
      throw new functions.https.HttpsError(
        "not-found",
        "No account found for this email address."
      );
    }
    throw error;
  }
}

exports.sendPasswordOtp = functions.https.onCall(async (request) => {
  const email = normalizeEmail(request.data?.email);
  if (!email || !email.includes("@")) {
    throw new functions.https.HttpsError("invalid-argument", "Enter a valid email address.");
  }

  await findUserByEmail(email);

  const otp = generateOtp();
  const expiresAt = Date.now() + OTP_TTL_MS;
  const docId = emailDocId(email);

  await admin.firestore().collection("password_otps").doc(docId).set({
    email,
    otp,
    expiresAt,
    createdAt: admin.firestore.FieldValue.serverTimestamp(),
  });

  const transport = getMailTransport();
  const from = process.env.OTP_EMAIL_USER;
  await transport.sendMail({
    from: `IRemember <${from}>`,
    to: email,
    subject: "Your IRemember password reset code",
    text:
      `Your IRemember password reset code is ${otp}.\n\n` +
      "This code expires in 10 minutes. If you did not request this, ignore this email.",
    html:
      `<p>Your IRemember password reset code is:</p>` +
      `<p style="font-size:28px;font-weight:bold;letter-spacing:4px;">${otp}</p>` +
      `<p>This code expires in 10 minutes. If you did not request this, ignore this email.</p>`,
  });

  return { success: true };
});

exports.verifyOtpAndResetPassword = functions.https.onCall(async (request) => {
  const email = normalizeEmail(request.data?.email);
  const otp = String(request.data?.otp || "").trim();
  const newPassword = String(request.data?.newPassword || "");

  if (!email || !email.includes("@")) {
    throw new functions.https.HttpsError("invalid-argument", "Enter a valid email address.");
  }
  if (!/^\d{6}$/.test(otp)) {
    throw new functions.https.HttpsError("invalid-argument", "Enter the 6-digit OTP from your email.");
  }
  if (newPassword.length < 8 || !/[A-Za-z]/.test(newPassword) || !/\d/.test(newPassword)) {
    throw new functions.https.HttpsError(
      "invalid-argument",
      "Password must be at least 8 characters with letters and numbers."
    );
  }

  const docId = emailDocId(email);
  const docRef = admin.firestore().collection("password_otps").doc(docId);
  const snapshot = await docRef.get();

  if (!snapshot.exists) {
    throw new functions.https.HttpsError("not-found", "OTP expired or not found. Request a new code.");
  }

  const data = snapshot.data();
  if (Date.now() > data.expiresAt) {
    await docRef.delete();
    throw new functions.https.HttpsError("deadline-exceeded", "OTP expired. Request a new code.");
  }
  if (data.otp !== otp) {
    throw new functions.https.HttpsError("invalid-argument", "Incorrect OTP. Please try again.");
  }

  const user = await findUserByEmail(email);
  await admin.auth().updateUser(user.uid, { password: newPassword });
  await docRef.delete();

  return { success: true };
});

function normalizeUsername(username) {
  return String(username || "").trim().toLowerCase();
}

function hashWithSalt(value, salt) {
  return crypto.createHash("sha256").update(`${salt}:${value}`).digest("hex");
}

function isStrongPassword(password) {
  return (
    String(password || "").length >= 8 &&
    /[A-Za-z]/.test(password) &&
    /\d/.test(password)
  );
}

async function loadUserDoc(username) {
  const normalized = normalizeUsername(username);
  if (!normalized) {
    throw new functions.https.HttpsError("invalid-argument", "Username is required.");
  }
  const docRef = admin.firestore().collection("users").doc(normalized);
  const snapshot = await docRef.get();
  if (!snapshot.exists) {
    throw new functions.https.HttpsError("not-found", "Account not found. Check your username.");
  }
  return { normalized, docRef, data: snapshot.data() };
}

function assertSecretMatches(data, secret) {
  if (!secret || !String(secret).trim()) {
    throw new functions.https.HttpsError("invalid-argument", "Secret details are required.");
  }
  const salt = data.secretSalt || "";
  const expected = data.secretHash || "";
  if (!salt || !expected) {
    throw new functions.https.HttpsError(
      "failed-precondition",
      "This account has no secret details set up."
    );
  }
  const actual = hashWithSalt(String(secret).trim(), salt);
  if (actual !== expected) {
    throw new functions.https.HttpsError(
      "permission-denied",
      "Incorrect secret details. Please try again."
    );
  }
}

exports.verifySecret = functions.https.onCall(async (request) => {
  const username = normalizeUsername(request.data?.username);
  const secret = String(request.data?.secret || "");
  const { data } = await loadUserDoc(username);
  assertSecretMatches(data, secret);
  return { success: true, username };
});

exports.resetPasswordWithSecret = functions.https.onCall(async (request) => {
  const username = normalizeUsername(request.data?.username);
  const secret = String(request.data?.secret || "");
  const newPassword = String(request.data?.newPassword || "");
  const newSecret = request.data?.newSecret != null ? String(request.data.newSecret) : null;

  if (!isStrongPassword(newPassword)) {
    throw new functions.https.HttpsError(
      "invalid-argument",
      "Password must be at least 8 characters with letters and numbers."
    );
  }

  const { normalized, docRef, data } = await loadUserDoc(username);
  assertSecretMatches(data, secret);

  const uid = data.firebaseUid;
  if (!uid) {
    throw new functions.https.HttpsError("failed-precondition", "Account is incomplete.");
  }

  await admin.auth().updateUser(uid, { password: newPassword });

  const passwordSalt = crypto.randomBytes(16).toString("hex");
  const passwordHash = hashWithSalt(newPassword, passwordSalt);
  const updates = {
    passwordSalt,
    passwordHash,
    updatedAt: admin.firestore.FieldValue.serverTimestamp(),
  };

  if (newSecret != null && String(newSecret).trim()) {
    const secretSalt = crypto.randomBytes(16).toString("hex");
    updates.secretSalt = secretSalt;
    updates.secretHash = hashWithSalt(String(newSecret).trim(), secretSalt);
  }

  await docRef.update(updates);
  return { success: true, username: normalized };
});
