#!/usr/bin/env bash
# Builds the iOS app for a Firebase Test Lab Game Loop test (see README.md in this folder).
#
# Usage: benchmark/testlab/build_ios.sh [--identity "Apple Development"] [--team TEAM_ID]
#                                       [--min-ios 16.0] [--simulator]
#
# Output: build/testlab/MsgVerify-testlab.ipa (signed, for Test Lab, which re-signs it), or with
# --simulator an unsigned build/testlab/MsgVerify-testlab-sim.app for a local check.
#
# The build is the Release configuration that run_ios.sh measures, with three differences that
# exist only in this build:
#  - ContextGuard is built from the source in this repository (interpreter-reuse fix), as for the
#    recorded results in benchmark/results/interpreter-fix/. The script checks this in the log.
#  - Info.plist registers the `firebase-game-loop` URL scheme, which starts the benchmark
#    (iosApp/TestLabGameLoop.swift). The file is patched temporarily and restored on exit.
#  - The minimum iOS version is lowered to --min-ios (default 16.0), because most Test Lab
#    iPhones run iOS 16. The code is unchanged, as with run_ios.sh --min-ios.
#
# Requirements: Xcode, CocoaPods, JDK 17, Android SDK (ContextGuard's Gradle build configures it).
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
IDENTITY="Apple Development"
TEAM_ID="${TEAM_ID:-}"
MIN_IOS="16.0"
SIMULATOR=0

while [[ $# -gt 0 ]]; do
  case "$1" in
    --identity) IDENTITY="$2"; shift 2 ;;
    --team) TEAM_ID="$2"; shift 2 ;;
    --min-ios) MIN_IOS="$2"; shift 2 ;;
    --simulator) SIMULATOR=1; shift ;;
    -h|--help) sed -n '2,20p' "$0"; exit 0 ;;
    *) echo "Unknown option: $1" >&2; exit 2 ;;
  esac
done

OUT="$ROOT/build/testlab"
DERIVED="$OUT/derived-ios"
LOG="$OUT/build_ios.log"
mkdir -p "$OUT"

export JAVA_HOME="${JAVA_HOME:-$(/usr/libexec/java_home -v 17 2>/dev/null || true)}"
export ANDROID_HOME="${ANDROID_HOME:-$HOME/Library/Android/sdk}"
# Read by settings.gradle.kts in the Gradle run of the Xcode build phase: build ContextGuard from source.
export ORG_GRADLE_PROJECT_msgverifyTestLocally=true
[[ -f "$ROOT/ContextGuard/gradle.properties" ]] || printf 'android.useAndroidX=true\n' > "$ROOT/ContextGuard/gradle.properties"

# pod install rewrites project.pbxproj cosmetically; both files are restored on exit.
PLIST="$ROOT/iosApp/iosApp/Info.plist"
PBXPROJ="$ROOT/iosApp/iosApp.xcodeproj/project.pbxproj"
cp "$PLIST" "$OUT/Info.plist.backup"
cp "$PBXPROJ" "$OUT/project.pbxproj.backup"
trap 'cp "$OUT/Info.plist.backup" "$PLIST"; cp "$OUT/project.pbxproj.backup" "$PBXPROJ"; echo "Restored Info.plist and project.pbxproj"' EXIT

echo "Installing CocoaPods dependencies..."
(cd "$ROOT/iosApp" && pod install --silent)
/usr/libexec/PlistBuddy \
  -c "Add :CFBundleURLTypes:1 dict" \
  -c "Add :CFBundleURLTypes:1:CFBundleURLName string firebase-game-loop" \
  -c "Add :CFBundleURLTypes:1:CFBundleURLSchemes array" \
  -c "Add :CFBundleURLTypes:1:CFBundleURLSchemes:0 string firebase-game-loop" "$PLIST"

if [[ "$SIMULATOR" -eq 1 ]]; then SDK=iphonesimulator; DEST="generic/platform=iOS Simulator"; else SDK=iphoneos; DEST="generic/platform=iOS"; fi
echo "Building Release for $SDK, minimum iOS $MIN_IOS (log: $LOG)..."
if ! xcodebuild -workspace "$ROOT/iosApp/iosApp.xcworkspace" -scheme iosApp -configuration Release \
    -sdk "$SDK" -destination "$DEST" -derivedDataPath "$DERIVED" \
    IPHONEOS_DEPLOYMENT_TARGET="$MIN_IOS" CODE_SIGNING_ALLOWED=NO TEAM_ID="$TEAM_ID" build > "$LOG" 2>&1; then
  grep -E 'error:|BUILD FAILED|FAILURE' "$LOG" | head -20 >&2
  exit 1
fi
# With the published ContextGuard the Gradle run has no :ContextGuard:library tasks.
grep -q ':ContextGuard:library:' "$LOG" ||
  { echo "ContextGuard was not built from source (no :ContextGuard:library tasks in $LOG)." >&2; exit 1; }

APP="$DERIVED/Build/Products/Release-$SDK/MsgVerify.app"
/usr/libexec/PlistBuddy -c 'Print :CFBundleURLTypes' "$APP/Info.plist" | grep -q firebase-game-loop ||
  { echo "The firebase-game-loop URL scheme is missing from the built app." >&2; exit 1; }
MINOS="$(vtool -show-build "$APP/MsgVerify" | awk '/minos/{print $2; exit}')"
echo "Built $APP (minos $MINOS)"

if [[ "$SIMULATOR" -eq 1 ]]; then
  rm -rf "$OUT/MsgVerify-testlab-sim.app" && cp -R "$APP" "$OUT/MsgVerify-testlab-sim.app"
  echo "Simulator app: $OUT/MsgVerify-testlab-sim.app"
  exit 0
fi

# Test Lab needs a signed app and re-signs it with its own certificate and provisioning profile,
# so the development identity is enough; no provisioning profile is embedded.
if [[ -n "$TEAM_ID" ]]; then
  BUNDLE_ID="$(/usr/libexec/PlistBuddy -c 'Print :CFBundleIdentifier' "$APP/Info.plist")"
  ENT="$OUT/entitlements.plist"
  cat > "$ENT" <<EOF
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0"><dict>
  <key>application-identifier</key><string>$TEAM_ID.$BUNDLE_ID</string>
  <key>com.apple.developer.team-identifier</key><string>$TEAM_ID</string>
  <key>get-task-allow</key><true/>
</dict></plist>
EOF
  ENT_ARGS=(--entitlements "$ENT")
else
  ENT_ARGS=()
fi
find "$APP" -depth \( -name '*.framework' -o -name '*.dylib' -o -name '*.appex' \) -print0 |
  while IFS= read -r -d '' item; do codesign --force --sign "$IDENTITY" --timestamp=none "$item"; done
codesign --force --sign "$IDENTITY" --timestamp=none "${ENT_ARGS[@]}" "$APP"
codesign --verify --deep --strict --verbose=1 "$APP"

rm -rf "$OUT/ipa" && mkdir -p "$OUT/ipa/Payload" && cp -R "$APP" "$OUT/ipa/Payload/"
(cd "$OUT/ipa" && rm -f ../MsgVerify-testlab.ipa && zip -qry ../MsgVerify-testlab.ipa Payload)
echo "IPA: $OUT/MsgVerify-testlab.ipa"
