#!/usr/bin/env bash
# Builds MsgVerify (Release), installs it on a connected iPhone, runs the benchmark,
# and copies the resulting CSV to benchmark/results/.
#
# Usage: benchmark/run_ios.sh --team TEAM_ID [--device UDID] [--passes N] [--delay-ms MS]
#                             [--bundle-id ID] [--min-ios VERSION] [--run-id ID] [--skip-build]
#                             [--start-delay SECONDS] [--timeout SECONDS]
#
# Requirements: Xcode 15+ (for xcrun devicectl), CocoaPods dependencies installed
# (cd iosApp && pod install), the iPhone connected, unlocked and trusted, Developer Mode on.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
PASSES=10
DELAY_MS=0
START_DELAY_S=60
DEVICE=""
TEAM_ID="${TEAM_ID:-}"
BUNDLE_ID_OVERRIDE=""
MIN_IOS=""
RUN_ID=""
SKIP_BUILD=0
TIMEOUT=1800

while [[ $# -gt 0 ]]; do
  case "$1" in
    --team) TEAM_ID="$2"; shift 2 ;;
    --device) DEVICE="$2"; shift 2 ;;
    --passes) PASSES="$2"; shift 2 ;;
    --delay-ms) DELAY_MS="$2"; shift 2 ;;
    --start-delay) START_DELAY_S="$2"; shift 2 ;;
    --bundle-id) BUNDLE_ID_OVERRIDE="$2"; shift 2 ;;
    --min-ios) MIN_IOS="$2"; shift 2 ;;
    --run-id) RUN_ID="$2"; shift 2 ;;
    --skip-build) SKIP_BUILD=1; shift ;;
    --timeout) TIMEOUT="$2"; shift 2 ;;
    -h|--help) sed -n '2,10p' "$0"; exit 0 ;;
    *) echo "Unknown option: $1" >&2; exit 2 ;;
  esac
done

RUN_ID="${RUN_ID:-ios-$(date +%Y%m%d-%H%M%S)}"
RESULTS="$ROOT/benchmark/results"
DERIVED="$ROOT/build/benchmark-ios"
mkdir -p "$RESULTS"

if [[ -z "$DEVICE" ]]; then
  json="$(mktemp)"
  xcrun devicectl list devices --json-output "$json" >/dev/null
  DEVICE="$(python3 -I - "$json" <<'PY'
import json, sys
devices = json.load(open(sys.argv[1]))["result"]["devices"]
for d in devices:
    hw = d.get("hardwareProperties", {})
    if hw.get("platform") == "iOS" and hw.get("reality") == "physical" \
            and d.get("connectionProperties", {}).get("tunnelState") != "unavailable":
        print(d["identifier"])
        break
PY
)"
  rm -f "$json"
  [[ -n "$DEVICE" ]] || { echo "No connected physical iOS device found (xcrun devicectl list devices)." >&2; exit 1; }
fi
# xcodebuild identifies devices by hardware UDID, which differs from the devicectl identifier.
json="$(mktemp)"
xcrun devicectl list devices --json-output "$json" >/dev/null
UDID="$(python3 -I - "$json" "$DEVICE" <<'PY'
import json, sys
for d in json.load(open(sys.argv[1]))["result"]["devices"]:
    if sys.argv[2] in (d["identifier"], d.get("hardwareProperties", {}).get("udid")):
        print(d["hardwareProperties"]["udid"])
        break
PY
)"
rm -f "$json"
[[ -n "$UDID" ]] || { echo "Could not find the hardware UDID of device $DEVICE." >&2; exit 1; }
echo "Device: $DEVICE (UDID $UDID)"

APP_DIR="$DERIVED/Build/Products/Release-iphoneos"
if [[ "$SKIP_BUILD" -eq 0 ]]; then
  [[ -n "$TEAM_ID" ]] || { echo "Pass --team TEAM_ID (Xcode > Settings > Accounts) to sign for the device." >&2; exit 2; }
  extra=()
  [[ -n "$BUNDLE_ID_OVERRIDE" ]] && extra+=("PRODUCT_BUNDLE_IDENTIFIER=$BUNDLE_ID_OVERRIDE")
  # Lower the app's deployment target for this build only, e.g. for a test device on an older iOS.
  # xcodebuild checks device eligibility against the project file and ignores command-line or
  # -xcconfig overrides, so the project file is patched temporarily and restored on exit.
  if [[ -n "$MIN_IOS" ]]; then
    PBXPROJ="$ROOT/iosApp/iosApp.xcodeproj/project.pbxproj"
    cp "$PBXPROJ" "$PBXPROJ.benchmark-backup"
    trap 'mv -f "$PBXPROJ.benchmark-backup" "$PBXPROJ"; echo "Restored project.pbxproj"' EXIT
    sed -i '' "s/IPHONEOS_DEPLOYMENT_TARGET = 17\.4;/IPHONEOS_DEPLOYMENT_TARGET = $MIN_IOS;/" "$PBXPROJ"
    extra+=("IPHONEOS_DEPLOYMENT_TARGET=$MIN_IOS")
    echo "Building with minimum iOS $MIN_IOS (project file patched temporarily)"
  fi
  # Building for the specific device lets Xcode register it with the team, which free
  # (Personal Team) accounts need before a provisioning profile can be created.
  echo "Building Release (Kotlin framework is built in release mode by the Xcode build phase)..."
  xcodebuild -workspace "$ROOT/iosApp/iosApp.xcworkspace" -scheme iosApp -configuration Release \
    -destination "id=$UDID" -derivedDataPath "$DERIVED" -allowProvisioningUpdates \
    TEAM_ID="$TEAM_ID" "${extra[@]}" build | tail -5
fi

APP="$(find "$APP_DIR" -maxdepth 1 -name '*.app' -print -quit 2>/dev/null || true)"
if [[ -n "$APP" ]]; then
  BUNDLE_ID="$(/usr/libexec/PlistBuddy -c 'Print :CFBundleIdentifier' "$APP/Info.plist")"
  if [[ "$SKIP_BUILD" -eq 0 ]]; then
    echo "Installing $APP"
    xcrun devicectl device install app --device "$DEVICE" "$APP" >/dev/null
  fi
else
  [[ -n "$BUNDLE_ID_OVERRIDE" ]] || { echo "No built app found; pass --bundle-id for an app installed another way." >&2; exit 1; }
  BUNDLE_ID="$BUNDLE_ID_OVERRIDE"
fi
echo "Bundle: $BUNDLE_ID  Run: $RUN_ID  Passes: $PASSES"

echo "Launching benchmark; it starts after ${START_DELAY_S}s. Unplug the cable now. Keep the iPhone unlocked with MsgVerify in the foreground until it finishes."
xcrun devicectl device process launch --device "$DEVICE" --terminate-existing "$BUNDLE_ID" -- \
  -msgverifyBenchmark -benchmarkPasses "$PASSES" -benchmarkRunId "$RUN_ID" -benchmarkDelayMs "$DELAY_MS" \
  -benchmarkStartDelayMs "$((START_DELAY_S * 1000))" >/dev/null

copy_from_device() {
  xcrun devicectl device copy from --device "$DEVICE" --domain-type appDataContainer \
    --domain-identifier "$BUNDLE_ID" --source "Documents/$1" --destination "$2" >/dev/null 2>&1
}

OUT="$RESULTS/benchmark-$RUN_ID.csv"
deadline=$((SECONDS + TIMEOUT))
while true; do
  if copy_from_device "benchmark-$RUN_ID.csv" "$OUT"; then
    echo "Saved $OUT ($(($(wc -l < "$OUT") - 1)) rows)"
    exit 0
  fi
  if copy_from_device "benchmark-$RUN_ID.failed" "$RESULTS/benchmark-$RUN_ID.failed"; then
    echo "Benchmark failed on the device:" >&2
    cat "$RESULTS/benchmark-$RUN_ID.failed" >&2
    exit 1
  fi
  if (( SECONDS > deadline )); then
    echo "Timed out after ${TIMEOUT}s waiting for benchmark-$RUN_ID.csv" >&2
    exit 1
  fi
  sleep 15
done
