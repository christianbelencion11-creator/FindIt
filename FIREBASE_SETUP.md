# Firebase Setup for IRemember

IRemember uses **Firebase Authentication** for email/password login, Google Sign-In, and **email OTP** password reset (via Cloud Functions). The Gradle SDK is already configured — you mainly need a valid `google-services.json` and deployed Cloud Functions for OTP.

**Current SDK versions (in `gradle/libs.versions.toml`):**

| Component | Version |
|-----------|---------|
| Kotlin | 2.3.0 |
| Google Services plugin | 4.5.0 |
| Firebase BoM | 34.15.0 |
| firebase-auth | via BoM |
| firebase-functions | via BoM |
| firebase-analytics | via BoM |

## 1. Create a Firebase project

1. Open [Firebase Console](https://console.firebase.google.com)
2. Use project `findit-73919` (or create a new one)
3. Google Analytics is optional

## 2. Add the Android app

1. Firebase → **Project settings** → **Your apps** → Android
2. Package name: `com.example.findit`
3. Download **`google-services.json`** → copy to `app/google-services.json`

> This file is **gitignored**. Do not commit it to GitHub.

## 3. Enable sign-in methods

Firebase Console → **Authentication** → **Sign-in method**:

| Provider | Action |
|----------|--------|
| **Email/Password** | Enable |
| **Google** | Enable, set support email |

## 4. Add SHA-1 fingerprint (required for Google Sign-In)

```powershell
cd c:\flutterprojects\IRemember
.\gradlew signingReport
```

Copy debug **SHA-1** → Firebase Console → Project settings → Android app → **Add fingerprint**

```
88:F6:6E:3E:DD:BC:BA:F8:12:33:86:77:E1:4A:A6:3D:40:EE:8F:27
```

Re-download `google-services.json` after adding SHA-1. Verify `oauth_client` is **not** empty.

## 5. Enable Firestore

Firebase Console → **Build** → **Firestore Database** → Create database (test mode is fine for development; deploy rules from `firestore.rules`).

OTP codes are stored in `password_otps/` — client apps cannot read them (see `firestore.rules`).

## 6. Deploy Cloud Functions (required for Forgot Password OTP)

Forgot password sends a **6-digit OTP email** via Cloud Functions in [`functions/index.js`](functions/index.js).

### One-time setup

```powershell
cd c:\flutterprojects\IRemember
npm install -g firebase-tools
firebase login
firebase use findit-73919
cd functions
npm install
cd ..
```

### Configure email sender (Gmail App Password recommended)

Create a [Gmail App Password](https://myaccount.google.com/apppasswords) for the sender account, then:

```powershell
firebase functions:config:set otp.user="your.sender@gmail.com" otp.pass="your-app-password"
```

Or set environment variables `OTP_EMAIL_USER` / `OTP_EMAIL_PASS` in Firebase Console → Functions → Environment variables.

### Deploy

```powershell
firebase deploy --only functions,firestore:rules
```

Functions deployed:
- `sendPasswordOtp` — validates email exists, stores OTP in Firestore, emails 6-digit code
- `verifyOtpAndResetPassword` — verifies OTP, updates password via Admin SDK

If functions are **not** deployed, the app shows: *"OTP service not configured — see FIREBASE_SETUP.md"*.

## 7. Build and run the Android app

```powershell
.\gradlew assembleDebug
```

Test checklist:
- **Register** with new email
- **Sign in** with email/password
- **Gmail** button → Google account picker (multicolor Google icon)
- **Forgot password** → Send OTP → enter code + new password → sign in
- **Switch Google account** → Home greeting updates to the new account name

## Troubleshooting

| Issue | Fix |
|-------|-----|
| Google Sign-In crashes | Ensure SHA-1 is added and `google-services.json` has `oauth_client` |
| App shows old name after new login | Sign out fully; profile resets when Firebase UID changes |
| OTP not received | Deploy functions; check spam; verify `otp.user` / `otp.pass` config |
| `OTP service not configured` | Run `firebase deploy --only functions` |
| Gmail button shows wrong icon | Rebuild app — uses `ic_google.xml` drawable |

## Moving to another PC

1. Clone repo + copy `app/google-services.json`
2. Add that machine's debug SHA-1 in Firebase Console
3. Re-download `google-services.json`
4. Deploy Cloud Functions if not already deployed on the project
5. Build and run

## Security note

Do not commit `google-services.json` or email credentials. OTP email passwords belong in Firebase Functions config/secrets only.
