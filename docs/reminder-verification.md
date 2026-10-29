# Reminder recovery checks

Install an updated debug or release build and open it once to register persistent recovery work.
Enable notifications and reminders, allow exact alarms, and choose a reminder window covering the next hour.

1. Close the app normally (do not use Android's Force stop). Confirm a reminder arrives.
2. Reboot, unlock the phone once, and leave the app closed. Confirm a reminder arrives within the configured window.
3. Leave notifications unread. Confirm `reminder-periodic-recovery` runs through Android Studio's Background Task Inspector and future alarms remain scheduled. Renewal runs roughly every 12 hours; Android may defer background work.
4. Change the timezone or device time. Confirm future alarms follow the new local reminder window.
5. Disable reminders. Confirm scheduled alarms are removed and recovery work does not re-enable them.
6. Test a window spanning midnight. Confirm after-midnight slots belong to the following date, with no moved or duplicated past slots.
7. Revoke exact alarm access, then grant it again. Confirm alarms are restored without opening the app.

Use `adb shell dumpsys alarm` and filter for `app.rork.quran` to inspect pending alarms.
Use Logcat tags `BootReceiver` and `ReminderRecovery` to inspect recovery failures.

Android's Force stop prevents background recovery until the app is opened again. Some phone vendors also require enabling Auto-start and unrestricted battery use. A minimal reminder plan and public verse payloads are mirrored in device-protected storage. Direct Boot receivers restore and deliver alarms without opening Room or DataStore, including before first unlock. Reading history stays in credential-protected storage.

## App improvements

- Change the daily reading goal and verify it does not change reminder frequency. Change reminder frequency (1–10) and verify it does not change the reading goal.
- Return from Android notification, battery, or alarm settings and verify the status refreshes. Block the notification channel as well as the app-wide permission.
- Send a test notification; verify it arrives without increasing reading progress. Tap it from a closed app and from Settings; both should show that verse on Home.
- Tap a normal notification: opening it must not increment progress. Press “قرأت” in the notification or “قرأت • التالية” on Home: a verse counts once per calendar day, including across app restarts.
- Increase Arabic font size to 44 and open a long verse in full-screen reading from Home and Bookmarks. Verify scrolling and closing work.
- Search bookmarks using Arabic without diacritics and with a surah name. Verify unified search, clear-search button, surah headings, verse ordering, no-results text, sharing, and removal.
- Upgrade an installed version-1 database and verify existing bookmarks and reading history remain intact.

Local pure Kotlin checks: `bash tests/run-domain-checks.sh` (set `KOTLINC` to a Kotlin compiler script if needed).
Android transaction and migration checks: run `:app:connectedDebugAndroidTest` with an emulator/device attached.


## HONOR / MagicOS reboot check

HONOR may block boot receivers and alarms unless App launch is configured manually. In Settings, search for App launch, turn off Manage automatically for this app, and enable Auto-launch, Secondary launch, and Run in background. Battery optimization exemption is a separate setting. Menu names can vary by MagicOS version. Official guidance: https://www.honor.com/uk/support/content/en-us00423197/

The reminder card includes this guidance on HONOR/HUAWEI devices. Its expandable recovery details compare the last received boot event with Android's current boot count; missing delivery is reported rather than presented as a successful recovery. The app cannot override Force stop or OEM launch restrictions.

Run Android tests for cached-plan restoration, stale-plan removal, and reschedule completion. For a real reboot delivery test on an isolated emulator, run `ReminderRecoveryTest#prepareRealRebootProbe` with instrumentation argument `rebootProbe=true`, reboot immediately, and verify the scheduled notification arrives without starting the activity. The probe is scheduled three minutes ahead; give slow emulators more time by changing that test-only offset.
