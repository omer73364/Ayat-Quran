#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
compiler="${KOTLINC:-/opt/android-studio/plugins/Kotlin/kotlinc/bin/kotlinc}"
output="$(mktemp -d)"
trap 'rm -rf "$output"' EXIT
bash "$compiler" app/src/main/java/app/rork/quran/domain/model/UserSettings.kt \
  app/src/main/java/app/rork/quran/domain/model/ArabicSearch.kt \
  app/src/main/java/app/rork/quran/notification/ReminderTimes.kt \
  tests/DomainRegression.kt -include-runtime -d "$output/checks.jar"
java -jar "$output/checks.jar"
