<center>

# بِسْمِ اللَّهِ الرَّحْمَنِ الرَّحِيمِ

## 📖 آيات قرآنية

</center>

<br/>

**A beautiful daily Quran reminder app for Android**

_Stay connected to the Quran — one ayah at a time_

![Android](https://img.shields.io/badge/Android-API%2026%2B-brightgreen?logo=android)
![Kotlin](https://img.shields.io/badge/Kotlin-1.9.24-7F52FF?logo=kotlin)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-2024.05-4285F4?logo=jetpackcompose)
![Hilt](https://img.shields.io/badge/Hilt-2.51.1-E84393)

---

## ✨ Features

- 🔔 **Smart Notifications** — Receive Quranic ayahs throughout the day at your preferred frequency, scheduled up to 7 days in advance
- 📚 **6,236 Ayahs** — Complete Quran text with authentic Arabic typography using the KFGQPC font
- 🔖 **Bookmarks** — Save your favorite ayahs and revisit them anytime
- 📊 **Progress Tracking** — Daily reading goals with a weekly chart to visualize your consistency
- 🌙 **Dark Mode** — Full dark/light theme support with smooth transitions
- 📤 **Share** — Share any ayah directly from the app
- ⚡ **Persistent Notifications** — Notifications survive device restarts and work across all major OEMs (Samsung, Xiaomi, Huawei, OnePlus)

---

## 🏗️ Architecture

Clean Architecture with clear separation of concerns:

```
app/
├── data/
│   ├── local/          # Room database, DAOs, entities
│   └── repository/     # Data sources & business logic
├── domain/
│   └── model/          # Pure Kotlin models
├── ui/
│   ├── component/      # Reusable Compose components
│   ├── navigation/     # NavGraph with slide animations
│   ├── screen/         # Home, Bookmarks, Settings
│   └── theme/          # Colors, typography, theming
├── notification/       # AlarmScheduler, BootReceiver, NotificationHelper
└── di/                 # Hilt modules
```

---

## 🛠️ Tech Stack

| Layer       | Technology                   |
| ----------- | ---------------------------- |
| Language    | Kotlin 1.9.24                |
| UI          | Jetpack Compose + Material 3 |
| Navigation  | Navigation Compose 2.7.7     |
| DI          | Hilt 2.51.1                  |
| Database    | Room 2.6.1                   |
| Preferences | DataStore 1.1.1              |
| Async       | Kotlin Coroutines 1.8.1      |
| JSON        | Gson 2.11.0                  |
| Build       | AGP 8.3.2, Gradle 8.5        |
| Min SDK     | API 26 (Android 8.0)         |

---

## 🚀 Getting Started

### Prerequisites

- Android Studio Hedgehog or later
- JDK 17
- Android SDK API 35

### Build & Run

1. Clone or download the project and open the root folder in Android Studio
2. Wait for Gradle sync to finish
3. Connect a device or start an emulator
4. Click **Run ▶** or press `Shift + F10`

### Release APK

1. **Build → Generate Signed Bundle / APK**
2. Choose **APK**
3. Select or create your keystore
4. Choose **release** build variant
5. Click **Finish**

---

## 📋 Permissions

| Permission                             | Reason                                        |
| -------------------------------------- | --------------------------------------------- |
| `SCHEDULE_EXACT_ALARM`                 | Schedule precise notification times           |
| `RECEIVE_BOOT_COMPLETED`               | Reschedule notifications after device restart |
| `POST_NOTIFICATIONS`                   | Show ayah notifications (Android 13+)         |
| `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` | Keep notifications working in background      |

---

## 🤝 Contributing

Pull requests are welcome. For major changes, please open an issue first.

---

Made with ❤️ for the Muslim community
