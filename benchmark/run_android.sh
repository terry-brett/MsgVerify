#!/usr/bin/env bash
# Builds the non-debuggable "benchmark" variant of MsgVerify, installs it on a connected Android
# device, runs the benchmark, and copies the resulting CSV to benchmark/results/.
#
# Usage: benchmark/run_android.sh [--serial SERIAL] [--passes N] [--delay-ms MS]
#                                 [--run-id ID] [--skip-build] [--timeout SECONDS]
#
# Requirements: Android SDK platform-tools (adb), USB debugging enabled, screen unlocked.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
PASSES=10
DELAY_MS=0
START_DELAY_S=60
SERIAL=""
RUN_ID=""
SKIP_BUILD=0
TIMEOUT=1800
PKG="com.terrydroid.msgverify"

while [[ $# -gt 0 ]]; do
  case "$1" in
    --serial) SERIAL="$2"; shift 2 ;;
    --passes) PASSES="$2"; shift 2 ;;
    --delay-ms) DELAY_MS="$2"; shift 2 ;;
    --start-delay) START_DELAY_S="$2"; shift 2 ;;
    --run-id) RUN_ID="$2"; shift 2 ;;
    --skip-build) SKIP_BUILD=1; shift ;;
    --timeout) TIMEOUT="$2"; shift 2 ;;
    -h|--help) sed -n '2,9p' "$0"; exit 0 ;;
    *) echo "Unknown option: $1" >&2; exit 2 ;;
  esac
done

ADB="${ADB:-$(command -v adb || echo "${ANDROID_HOME:-$HOME/Library/Android/sdk}/platform-tools/adb")}"
if [[ -n "$SERIAL" ]]; then export ANDROID_SERIAL="$SERIAL"; fi
"$ADB" get-state >/dev/null || { echo "No Android device connected (adb devices)." >&2; exit 1; }

RUN_ID="${RUN_ID:-android-$(date +%Y%m%d-%H%M%S)}"
RESULTS="$ROOT/benchmark/results"
mkdir -p "$RESULTS"

if [[ "$SKIP_BUILD" -eq 0 ]]; then
  echo "Building and installing the benchmark variant..."
  (cd "$ROOT" && ./gradlew :composeApp:installBenchmark --console=plain -q)
fi

REMOTE="/sdcard/Android/data/$PKG/files"
echo "Run: $RUN_ID  Passes: $PASSES"
echo "Launching benchmark; it starts after ${START_DELAY_S}s. Unplug the cable now. Keep the screen on with MsgVerify in the foreground until it finishes."
"$ADB" shell am force-stop "$PKG"
"$ADB" shell am start -n "$PKG/.MainActivity" \
  --ez msgverify_benchmark true --ei msgverify_passes "$PASSES" \
  --es msgverify_run_id "$RUN_ID" --el msgverify_delay_ms "$DELAY_MS" \
  --el msgverify_start_delay_ms "$((START_DELAY_S * 1000))" >/dev/null

OUT="$RESULTS/benchmark-$RUN_ID.csv"
deadline=$((SECONDS + TIMEOUT))
while true; do
  if "$ADB" shell ls "$REMOTE/benchmark-$RUN_ID.csv" >/dev/null 2>&1; then
    "$ADB" pull "$REMOTE/benchmark-$RUN_ID.csv" "$OUT" >/dev/null
    echo "Saved $OUT ($(($(wc -l < "$OUT") - 1)) rows)"
    exit 0
  fi
  if "$ADB" shell ls "$REMOTE/benchmark-$RUN_ID.failed" >/dev/null 2>&1; then
    echo "Benchmark failed on the device:" >&2
    "$ADB" shell cat "$REMOTE/benchmark-$RUN_ID.failed" >&2
    exit 1
  fi
  if (( SECONDS > deadline )); then
    echo "Timed out after ${TIMEOUT}s waiting for benchmark-$RUN_ID.csv" >&2
    exit 1
  fi
  sleep 10
done
