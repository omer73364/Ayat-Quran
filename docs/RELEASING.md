# Building a signed release

Production builds read signing credentials from the ignored root file `signing.local.properties`:

```properties
storeFile=.signing/ayat-release.jks
keyAlias=ayat-release
storePassword=YOUR_STORE_PASSWORD
keyPassword=YOUR_KEY_PASSWORD
```

Keep the keystore and credentials private. Back up both to a secure location outside this checkout before distributing releases. They are required to sign future updates, and are intentionally excluded from Git. Never upload them as release assets.

Increase `versionCode` and `versionName` in `app/build.gradle.kts`, then run `:app:assembleRelease` and `:app:lintRelease` with Gradle 8.5 and JDK 17. Run `bash tests/run-domain-checks.sh` for scheduling and search checks.

When local signing configuration is absent, the release APK is unsigned. Verify the resulting APK with Android SDK `apksigner verify --verbose --print-certs`, check its application ID and version, and publish only the signed APK. Preserve the release mapping file privately for crash diagnostics.

The v1.1.0 production key differs from the earlier debug key. Android requires uninstalling a debug-signed installation before moving to this key; uninstalling removes the app's local data.
