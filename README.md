# IRemember 📝🔍

**IRemember** is a modern, native Android offline-first item tracking and reminder application built using **Jetpack Compose**, **Kotlin**, **Room Database**, and **Material 3**. It is designed to help users track personal belongings, store cards, organize notes, set contextual alerts and reminders, and quickly search or recall where things are kept.

---

## 📱 Features

- **Item Tracking & Categorization**: Record belongings with locations, photos, reminders, and tags.
- **Smart Reminders & Notifications**: Built-in alarms and work-manager scheduled notifications for items and notes.
- **Card Organizer**: Securely manage digital cards and details with biometric protection support.
- **Notes & Checklists**: Create structured notes with checklist items and reminder dates.
- **Search & Quick Retrieval**: Instant search and voice search capabilities to locate items quickly.
- **Dark & Light Mode**: Seamless adaptive theme support tailored for modern Android devices.
- **Local & Offline Privacy**: All data is stored locally on the device using SQLite Room database without external tracking.

---

## 🛠️ Tech Stack & Architecture

- **Language**: Kotlin 2.3.0
- **UI Toolkit**: Jetpack Compose with Material 3
- **Local Database**: Room 2.8.4 + KSP
- **Image Loading**: Coil Compose 2.7.0
- **Background Work**: AndroidX WorkManager 2.10.0
- **Security & Auth**: AndroidX Biometric & Security Crypto
- **Minimum SDK**: Android API 24 (Android 7.0 Nougat)
- **Target SDK**: Android API 36

---

## 🚀 Getting Started

### Prerequisites
- **Android Studio Ladybug | 2024.2+** or newer
- **JDK 11** or **JDK 17** (configured in Android Studio under `Settings > Build, Execution, Deployment > Build Tools > Gradle`)
- Android device or emulator running API 24+

### Clone and Run

```bash
# 1. Clone repository
git clone https://github.com/christianbelencion11-creator/FindIt.git IRemember

# 2. Open project in Android Studio
# Select "Open" and navigate to the cloned IRemember directory

# 3. Sync Gradle
# Android Studio will automatically run Gradle Sync.
# Or click File > Sync Project with Gradle Files

# 4. Run the app
# Select the 'app' configuration and choose your device/emulator, then press Run (Shift + F10)
```

---

## ⚠️ Troubleshooting Guide for Classmates / Collaborators

If you encounter build errors such as:
```text
Conflicting overloads: fun AlertsScreen(...)
Conflicting overloads: fun SearchScreen(...)
```

### Bakit ito nangyayari? (Why does this happen?)
1. **Duplicate Files**: May mga na-download o na-copy-paste na files sa project na may suffix na `(1).kt` (halimbawa: `AlertsScreen(1).kt`, `SearchScreen(1).kt`). Dahil may dalawang file na may parehong function signature sa iisang package, nagkakaroon ng `Conflicting overloads`.
2. **Old Package Directory**: Kung nag-pull ka mula sa lumang bersyon kung saan ang package name ay `com.example.findit`, ang mga hindi naka-track o duplicate na files ay maaaring naiwan sa lumang `app/src/main/java/com/example/findit/` folder. Ang opisyal na package name na ngayon ay **`com.example.iremember`**.

### Paano Ayusin (How to Fix):

#### Paraan 1: Fresh Clone (Pinakamabilis at Sigurado)
1. I-delete o i-rename ang lumang folder sa iyong computer.
2. Mag-clone ng panibago:
   ```bash
   git clone https://github.com/christianbelencion11-creator/FindIt.git
   ```
3. Buksan ang bagong na-clone na folder sa Android Studio.

#### Paraan 2: Linisin ang Existing Folder
1. Pumunta sa:
   `app/src/main/java/com/example/`
2. Kung may nakikita ka pang folder na **`findit`**, i-delete ito nang buo. (Dapat **`iremember`** lamang ang folder sa loob ng `com/example/`).
3. Siguraduhing walang anumang file na nagtatapos sa `(1).kt`.
4. Sa Android Studio:
   - I-click ang **File > Sync Project with Gradle Files**
   - I-click ang **Build > Clean Project**
   - I-click ang **Build > Rebuild Project**
