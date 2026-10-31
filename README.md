# joitonnessa – ঔষধ ও খাদ্য (Medicine & Food Reminder Assistant)

**joitonnessa** is a native, offline-first Android application designed to help users accurately remember when to take prescribed medicines, eat timely meals, drink water, and maintain consistent daily health routines through reliable scheduled alarms, notifications, and reminders.

---

## 🌟 Key Features

1. **Personalized Dashboard**:
   - Greeting with current date and daily progress indicator.
   - Status counters for Completed (সম্পন্ন), Pending (অপেক্ষমান), and Missed (মিস) reminders.
   - Quick-add actions for medicines and meals.
   - Integrated Water Tracker card with quick-log buttons (`+250 ml`, `+500 ml`).
   - Battery optimization guidance notice.

2. **Comprehensive Medicine Management**:
   - Full CRUD support: Add, Edit, View Details, and Delete medicines.
   - Medicine types: Tablet, Capsule, Syrup, Drops, Injection, Other.
   - Food relationship instructions: Before food (খাবারের আগে), After food (খাবারের পরে), With food (খাবারের সাথে), No restriction (খাবারের নিয়ম নেই).
   - Multiple custom reminder times per medicine.
   - Day-of-week recurrence selection (Every day, Weekdays, Custom).
   - Strictly advisory: Does **not** alter or recommend medication doses.

3. **Food & Meal Reminders**:
   - Breakfast, Lunch, Dinner, Evening Snacks, and custom meals.
   - Configurable recurring schedules and reminder sounds/vibrations.
   - Helps coordinate medicine intake that requires full or empty stomachs.

4. **Hydration & Water Tracking**:
   - Daily water goal presets (1500ml to 3500ml).
   - Interval-based reminder alerts (45, 60, 90, 120 minutes).
   - Real-time intake progress bar and daily consumption log.
   - Can be toggled on/off at any time.

5. **Smart Alarm & Notification Engine**:
   - Exact alarms using Android `AlarmManager` with `setExactAndAllowWhileIdle()`.
   - Android 13+ runtime notification permissions and Android 12+ exact alarm permissions.
   - `BootReceiver` that automatically reconciles and restores all active alarms upon device restart, app update, or timezone change.
   - Actionable notifications: **Taken (খেয়েছি)**, **Snooze 10m (১০ মি. পরে)**, **Skip (বাদ দিন)**.
   - Full-screen interactive **Alarm Ringing Activity** with ringtone and vibration.

6. **History, Adherence & Reports**:
   - 14-day interactive calendar history browser.
   - Filter by Taken, Skipped, and Missed.
   - Adherence analytics with custom Compose Canvas weekly bar charts.
   - Full user control to edit or delete any past history entry.

7. **Settings & Privacy**:
   - Bilingual support: **Bangla (বাংলা)** and **English**.
   - Theme options: System Default, Light Mode, Dark Mode.
   - 100% Offline-First local storage powered by Room Database.

---

## 🛠️ Technology Stack & Architecture

- **Language**: Kotlin 2.2
- **UI Framework**: Jetpack Compose with Material 3 Design
- **Architecture**: MVVM with Clean Architecture separation
- **Local Persistence**: Android Jetpack Room Database with KSP
- **Preferences**: Jetpack DataStore Preferences
- **Scheduling**: Android `AlarmManager` + `BroadcastReceiver` + `NotificationManager`
- **Minimum SDK**: Android 8.0 (API Level 26)
- **Target SDK**: Android 15 (API Level 36)

---

## 🚀 How to Open in Android Studio

1. Open **Android Studio** (Hedgehog, Iguana, Jellyfish, or newer).
2. Select **File > Open...** and navigate to the root directory of this project.
3. Allow Gradle to synchronize dependencies.
4. Connect an Android device (Android 8.0 or newer) with USB Debugging enabled, or start an Android Virtual Device (AVD).
5. Click **Run > Run 'app'** (`Shift + F10`).

---

## 📦 Generating APKs

### 1. Build Debug APK:
In the project terminal or shell:
```bash
gradle :app:assembleDebug
```
The output APK will be located at:
`app/build/outputs/apk/debug/app-debug.apk`

### 2. Build Release APK:
```bash
gradle :app:assembleRelease
```
The release APK will be located at:
`app/build/outputs/apk/release/app-release-unsigned.apk` (or signed if keystore is configured).

### 3. Signing Release APK Guidance:
To sign your release APK for Google Play or direct distribution:
```bash
keytool -genkey -v -keystore release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias joitonnessa
apksigner sign --ks release-key.jks --out app-release-signed.apk app-release-unsigned.apk
```

---

## 🧪 Testing

Run JVM unit and Robolectric database tests:
```bash
gradle :app:testDebugUnitTest
```
Tests verify:
- Accurate `DateTimeUtils` time calculation and bilingual digit translation.
- In-memory Room database insertion, querying, and updating.
- String resource validation.
