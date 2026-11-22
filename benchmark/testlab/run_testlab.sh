#!/usr/bin/env bash
# Runs the MsgVerify benchmark on Firebase Test Lab physical devices and copies the results to
# benchmark/results/testlab/ (see README.md in this folder).
#
# Usage: benchmark/testlab/run_testlab.sh [--project ID] [--android | --ios] [--skip-build]
#                                         [--device SPEC]... [--team TEAM_ID] [--runs N]
#                                         [--fetch-only gs://BUCKET/RESULTS_DIR]
#
# Devices: ANDROID_DEVICES and IOS_DEVICES below, or --device (with --android or --ios) to run
# other devices, e.g. --ios --device model=iphone8,version=16.6. List them with
# `gcloud firebase test android models list` and `gcloud firebase test ios models list`.
# Requirements: gcloud with the beta component, signed in, and a Firebase project on the Blaze plan
# (the no-cost Spark plan allows only five physical-device runs per day).
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
HERE="$ROOT/benchmark/testlab"
PROJECT="${PROJECT:-$(gcloud config get-value project 2>/dev/null)}"
TEAM_ID="${TEAM_ID:-}"
RUNS=3
PLATFORMS="android ios"
SKIP_BUILD=0
FETCH_ONLY=""
DEVICE_OVERRIDE=()

ANDROID_DEVICES=("model=a16,version=35")              # Samsung Galaxy A16
IOS_DEVICES=(
  "model=iphone8,version=16.6"
  "model=iphone11pro,version=16.6"
  "model=iphone14pro,version=16.6"
  "model=iphone16pro,version=18.3"
)

while [[ $# -gt 0 ]]; do
  case "$1" in
    --project) PROJECT="$2"; shift 2 ;;
    --android) PLATFORMS="android"; shift ;;
    --ios) PLATFORMS="ios"; shift ;;
    --skip-build) SKIP_BUILD=1; shift ;;
    --team) TEAM_ID="$2"; shift 2 ;;
    --runs) RUNS="$2"; shift 2 ;;
    --fetch-only) FETCH_ONLY="$2"; shift 2 ;;
    --device) DEVICE_OVERRIDE+=("$2"); shift 2 ;;
    -h|--help) sed -n '2,12p' "$0"; exit 0 ;;
    *) echo "Unknown option: $1" >&2; exit 2 ;;
  esac
done
[[ -n "$PROJECT" ]] || { echo "Pass --project or run: gcloud config set project <ID>" >&2; exit 2; }
if [[ ${#DEVICE_OVERRIDE[@]} -gt 0 ]]; then
  case "$PLATFORMS" in
    android) ANDROID_DEVICES=("${DEVICE_OVERRIDE[@]}") ;;
    ios) IOS_DEVICES=("${DEVICE_OVERRIDE[@]}") ;;
    *) echo "--device needs --android or --ios" >&2; exit 2 ;;
  esac
fi

OUT="$ROOT/build/testlab"
RESULTS="$ROOT/benchmark/results/testlab"
mkdir -p "$OUT" "$RESULTS"
STAMP="$(date +%Y%m%d-%H%M%S)"

# Copies the benchmark files of one Test Lab results directory into benchmark/results/testlab/.
fetch() {
  local url="$1" files
  files="$(gcloud storage ls "$url/**" 2>/dev/null |
    grep -E '/(benchmark-testlab-[^/]+\.(csv|failed)|conditions-testlab-[^/]+\.txt)$' || true)"
  if [[ -z "$files" ]]; then echo "No benchmark files found under $url" >&2; return 1; fi
  # Test Lab uploads Documents/GameLoopResults on iOS by itself; keep one copy of each file name.
  echo "$files" | awk -F/ '!seen[$NF]++' | while read -r f; do
    gcloud storage cp --quiet "$f" "$RESULTS/" >/dev/null && echo "  $RESULTS/$(basename "$f")"
  done
}

# Returns the gs:// results URL that gcloud printed for a test matrix.
results_url() {
  sed -nE 's#.*storage/browser/([^]/]+)/([^]]+)/?\].*#gs://\1/\2#p' "$1" | head -1 | sed 's#/$##'
}

if [[ -n "$FETCH_ONLY" ]]; then fetch "$FETCH_ONLY"; exit $?; fi

pids=()
for platform in $PLATFORMS; do
  log="$OUT/testlab-$platform-$STAMP.log"
  if [[ "$platform" == "android" ]]; then
    [[ "$SKIP_BUILD" -eq 1 ]] || "$HERE/build_android.sh"
    devices=(); for d in "${ANDROID_DEVICES[@]}"; do devices+=(--device "$d"); done
    cmd=(gcloud firebase test android run --project "$PROJECT" --type instrumentation
      --app "$OUT/MsgVerify-benchmark.apk" --test "$OUT/MsgVerify-testlab-driver.apk"
      --test-targets "class com.terrydroid.msgverify.testlab.TestLabBenchmark"
      --environment-variables "runs=$RUNS,passes=10,startDelayS=60"
      --directories-to-pull /data/local/tmp/msgverify-benchmark
      --results-dir "msgverify-benchmark-android-$STAMP"
      --timeout 20m --no-record-video --no-performance-metrics "${devices[@]}")
  else
    [[ "$SKIP_BUILD" -eq 1 ]] || "$HERE/build_ios.sh" ${TEAM_ID:+--team "$TEAM_ID"}
    scenarios="$(seq -s, 1 "$RUNS")"
    devices=(); for d in "${IOS_DEVICES[@]}"; do devices+=(--device "$d"); done
    cmd=(gcloud beta firebase test ios run --project "$PROJECT" --type game-loop
      --app "$OUT/MsgVerify-testlab.ipa" --scenario-numbers "$scenarios"
      --results-dir "msgverify-benchmark-ios-$STAMP"
      --timeout 20m --no-record-video "${devices[@]}")
  fi
  echo "Starting $platform on Test Lab (log: $log)"
  { echo "+ ${cmd[*]}"; "${cmd[@]}"; } > "$log" 2>&1 &
  pids+=("$!:$platform:$log")
done

status=0
for entry in "${pids[@]}"; do
  IFS=: read -r pid platform log <<< "$entry"
  if wait "$pid"; then echo "$platform: Test Lab finished"; else echo "$platform: Test Lab reported a failure (see $log)" >&2; status=1; fi
  url="$(results_url "$log")"
  if [[ -n "$url" ]]; then
    echo "$platform: copying results from $url"
    fetch "$url" || status=1
  else
    echo "$platform: no results URL in $log" >&2; status=1
  fi
done
exit "$status"
