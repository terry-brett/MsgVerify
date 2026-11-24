#!/usr/bin/env bash
# Builds the two APKs for the Firebase Test Lab Android benchmark (see README.md in this folder).
#
# Usage: benchmark/testlab/build_android.sh
#
# Output in build/testlab/:
#   MsgVerify-benchmark.apk   the app, "benchmark" build type (release settings, not debuggable),
#                             the same build that run_android.sh measures
#   MsgVerify-testlab-driver.apk   the instrumentation test that starts the runs on the device
#
# ContextGuard is built from the source in this repository (interpreter-reuse fix), as for the
# recorded results in benchmark/results/interpreter-fix/. The script checks this in the APK.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
OUT="$ROOT/build/testlab"
LOG="$OUT/build_android.log"
mkdir -p "$OUT"

export JAVA_HOME="${JAVA_HOME:-$(/usr/libexec/java_home -v 17 2>/dev/null || true)}"
export ANDROID_HOME="${ANDROID_HOME:-$HOME/Library/Android/sdk}"
[[ -f "$ROOT/ContextGuard/gradle.properties" ]] || printf 'android.useAndroidX=true\n' > "$ROOT/ContextGuard/gradle.properties"

echo "Building the benchmark app and the Test Lab driver (log: $LOG)..."
if ! (cd "$ROOT" && ./gradlew --console=plain -PmsgverifyTestLocally=true \
    :composeApp:assembleBenchmark :benchmarkTestLab:assembleBenchmark) > "$LOG" 2>&1; then
  grep -E '^e: |error:|What went wrong' -A3 "$LOG" | head -20 >&2
  exit 1
fi
grep -q ':ContextGuard:library:' "$LOG" ||
  { echo "ContextGuard was not built from source (no :ContextGuard:library tasks in $LOG)." >&2; exit 1; }

cp "$ROOT/composeApp/build/outputs/apk/benchmark/composeApp-benchmark.apk" "$OUT/MsgVerify-benchmark.apk"
cp "$ROOT/benchmark/testlab/android/build/outputs/apk/benchmark/benchmarkTestLab-benchmark.apk" \
  "$OUT/MsgVerify-testlab-driver.apk"

# The interpreter-reuse fix adds a mutex field to both TFLite wrappers. Check it in the APK.
DEXDUMP="$(ls -d "$ANDROID_HOME"/build-tools/*/dexdump 2>/dev/null | sort -V | tail -1 || true)"
if [[ -n "$DEXDUMP" ]]; then
  tmp="$(mktemp -d)"
  unzip -q -o "$OUT/MsgVerify-benchmark.apk" 'classes*.dex' -d "$tmp"
  found="$(for d in "$tmp"/classes*.dex; do "$DEXDUMP" -l plain "$d" 2>/dev/null; done |
    LC_ALL=C awk '/Class descriptor/{c=$0} /name *: .mutex./{ if (c ~ /(UrlVerifier|MessageVerifier);/) print c }' |
    sort -u | wc -l | tr -d ' ')"
  rm -rf "$tmp"
  [[ "$found" == "2" ]] ||
    { echo "The interpreter-reuse fix is missing from the APK (found $found of 2 classes)." >&2; exit 1; }
  echo "Checked: interpreter-reuse fix present in UrlVerifier and MessageVerifier."
fi
echo "App:    $OUT/MsgVerify-benchmark.apk"
echo "Driver: $OUT/MsgVerify-testlab-driver.apk"
