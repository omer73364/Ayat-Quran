# Verification — 29 October 2026

- Debug APK built successfully with Gradle 8.5 and Android SDK 35.
- The app and Android test package compiled successfully.
- All eight Android instrumentation tests passed on the API 35 emulator: four reading-progress/migration tests, three reminder-plan tests (the opt-in reboot probe is a no-op in a normal test run), and one rescheduling completion test.
- The rescheduling test confirms loading finishes and the next reminder updates without navigating away.
- The saved screen was visually checked in dark mode at 360 dp width: one rounded search field, no checkbox, and grouped saved verses.
- Reader layout was inspected and its height was subsequently constrained to the host's usable screen area to keep font controls above system navigation. The last height adjustment compiled successfully; its final visual check is still outstanding.
- A real reboot probe was prepared successfully, but the emulator disconnected before its notification outcome could be collected. This is **not** a passed reboot-delivery test.

The API 35 emulator does not reproduce HONOR MagicOS 10 launch restrictions. Physical-device reboot validation remains necessary on the HONOR Magic7 Pro / Android 16. In App launch settings, disable automatic management for this app and allow Auto-launch, Secondary launch, and Run in background. The app provides this guidance and boot-event diagnostics; it cannot override OEM restrictions or Android Force stop.
