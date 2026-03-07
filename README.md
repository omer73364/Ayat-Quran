# بِسْمِ اللَّهِ الرَّحْمَنِ الرَّحِيمِ

## 📖 آيات قرآنية

**A beautiful daily Quran reminder app for Android**

_Stay connected to the Quran — one ayah at a time_

![Android](https://img.shields.io/badge/Android-API%2026%2B-brightgreen?logo=android)
![Kotlin](https://img.shields.io/badge/Kotlin-1.9.24-7F52FF?logo=kotlin)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-2024.05-4285F4?logo=jetpackcompose)
![Hilt](https://img.shields.io/badge/Hilt-2.51.1-E84393)

---

## 📱 Screenshots

<p float="left">
  <img src="https://github.com/user-attachments/assets/57bcf7d4-969c-469a-a05e-5288b93e8607" width="13%" />
  <img src="https://github.com/user-attachments/assets/01396621-4deb-4ff2-a8bf-c9f0e8396ba3" width="13%" />
  <img src="https://github.com/user-attachments/assets/4d08cc9f-d90b-4a2e-b36f-f773b68b51dc" width="13%" />
  <img src="https://github.com/user-attachments/assets/7e7b2d7d-59d9-433e-97cc-9d6a8fb375bf" width="13%" />
  <img src="https://github.com/user-attachments/assets/d6a89fe3-c71f-4227-a8f6-96c730884907" width="13%" />
  <img src="https://github.com/user-attachments/assets/df350b7f-6bf8-4904-85e5-6cb62b2a2dd3" width="13%" />
  <img src="https://github.com/user-attachments/assets/b78e410b-a867-4875-924c-57366062a42f" width="13%" />
</p>

---

## ✨ Features

- 🔔 **Smart Notifications** — Receive Quranic ayahs throughout the day at your preferred frequency, scheduled up to 7 days in advance
- 📚 **6,236 Ayahs** — Complete Quran text with authentic Arabic typography using the KFGQPC font
- 🔖 **Bookmarks** — Save your favorite ayahs and revisit them anytime
- 📊 **Progress Tracking** — Daily reading goals with a weekly chart to visualize your consistency
- 🌙 **Dark Mode** — Full dark/light theme support with smooth transitions
- 📤 **Share** — Share any ayah directly from the app

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
