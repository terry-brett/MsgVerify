# MsgVerify benchmark

Measures the on-device cost of one ContextGuard analysis (`ContentVerifier.verify`) on Android and iOS: latency per message and process memory. The scripts here build the app, run the benchmark on a connected physical device, copy the results back as CSV, and analyse them.

## What is measured

- **Corpus:** the 90 bundled demo messages in `mocks/` (30 per channel: SMS, email, social media), processed in a fixed order that is identical on both platforms. Each message has a stable `message_id`, so the two platforms can be compared message by message.
- **Latency:** wall-clock time of one `verify` call. That covers URL extraction and classification, BERT text classification, heuristic labelling and the platform spellcheck. No warm-up is discarded. The first inference of the process is recorded with `phase = cold_start`, and the analysis reports it separately.
- **Memory:** sampled after every inference, outside the timed region, plus one `baseline` sample before any model has loaded. The primary metric, `mem_footprint_bytes`, covers the whole process on both platforms: total PSS on Android, `phys_footprint` on iOS. The CSV also records resident size, and on Android the managed (Java) heap and the native heap.
- **Context:** device model, OS version, thermal state, and whether the build is debuggable or running on a simulator/emulator. The analysis excludes simulator and debug rows by default, because those timings don't reflect real devices.

## Code under test

The recorded results in `results/interpreter-fix/` were measured with ContextGuard built from the source in this repository, including the interpreter-reuse fix for the iOS memory leak. Until that fix is published as a ContextGuard release, set `testLocally = true` in `settings.gradle.kts` to reproduce them, and create `ContextGuard/gradle.properties` (gitignored) containing `android.useAndroidX=true`; otherwise the app builds against the published 1.0.3. See `results/README.md` for which code each result set measured.

## Running

Use physical devices. Emulators and simulators run on the host computer's CPU, and their timings and memory are not representative.

Use the same protocol on every device:

- Airplane mode on. This also shows that the analysis needs no network.
- Battery above 50 %, not charging. Low Power Mode / Battery Saver off. Other apps closed.
- Let the device cool to room temperature before the first run.
- Three separate runs of 10 passes each. Every script invocation restarts the app, so each run contributes one cold-start measurement.
- Keep the device unlocked with MsgVerify in the foreground until the script reports the CSV.

```bash
for i in 1 2 3; do benchmark/run_android.sh --serial <SERIAL> --passes 10; done
```

### iOS

```bash
cd iosApp && pod install && cd ..            # once
benchmark/run_ios.sh --team <TEAM_ID> --passes 10
```

- **Build:** the script builds the Release configuration, so the Kotlin framework is optimised. It installs the app with `xcrun devicectl` (Xcode 15+), launches it in benchmark mode, and copies `benchmark-<run-id>.csv` from the app's Documents folder into `benchmark/results/`.
- **Bundle ID clash:** if signing fails because the bundle identifier belongs to another team, add `--bundle-id <your.unique.id>`.
- **Older iOS on the test device:** the project's minimum iOS version is 17.4. To benchmark a device on an older iOS, add `--min-ios <version>` (for example `--min-ios 17.0`). This lowers the minimum for that build only. The script patches `project.pbxproj` temporarily, because Xcode checks device compatibility against the project file and ignores command-line overrides, and restores the file when it exits.
- **Your own build:** to use an app you installed yourself from Xcode (Release configuration), pass `--skip-build --bundle-id <id>`.

### Android

```bash
benchmark/run_android.sh --passes 10
```

The script installs the `benchmark` build type, which uses release settings, is not debuggable, and is signed with the debug key. It launches the benchmark through intent extras and pulls the CSV with `adb`. Use `--serial` when more than one device is connected.

### Options (both scripts)

| Option | Default | Meaning |
|---|---|---|
| `--passes N` | 10 | Passes over the 90-message corpus |
| `--delay-ms MS` | 0 | Pause after each message |
| `--start-delay S` | 60 | Wait after launch before measuring, so the cable can be unplugged |
| `--run-id ID` | platform + timestamp | Name of the run and of the CSV |
| `--timeout S` | 1800 | Give up waiting for the CSV after S seconds |

## Analysis

```bash
python3 -m venv .venv && .venv/bin/pip install -r benchmark/requirements.txt
.venv/bin/python benchmark/analyze.py benchmark/results/interpreter-fix/*.csv
```

This writes the following to `benchmark/results/summary/`:

- `latency_by_device.csv`: per device and channel, pooled over runs, the median, IQR, 95th percentile, mean and SD of steady-state latency, and the share of inferences under 100 ms. `latency_by_run.csv` has the same statistics per run.
- `cold_start.csv`: the first inference of each run.
- `paired_tests.csv`: every iOS device against every Android device, paired by message. For each message, the analysis takes the median over all passes and runs on each device, and compares the two with a two-sided Wilcoxon signed-rank test per channel. Effect size is the matched-pairs rank-biserial correlation, and p-values are Holm-Bonferroni adjusted across all device pairs and channels.
- `memory_summary.csv`: per run, the baseline, after-first-inference, median and peak memory for each metric, plus the thermal states observed.
- `summary.md`: all tables above in Markdown.
- `latency.pdf`: one row per device, one column per channel.
- `memory.pdf`: footprint across each run, one panel per device.

## Known issue

ContextGuard waits for the platform spellchecker with no timeout. On a device whose spell-checker service doesn't respond, each verification blocks indefinitely: the `MSGVERIFY_BENCHMARK_PROGRESS` lines in the device log stop advancing. This has been seen on a headless Android emulator.

On Android 11 (seen on a OnePlus 6T), `Debug.getMemoryInfo` returns the same cached total PSS for every sample of a run, while resident size and the heap figures vary normally. Android caches this value for several minutes, longer than a run. `analyze.py` detects a footprint that never changes during a run and excludes it from the memory results with a warning. Reading PSS from `/proc/self/smaps_rollup` instead would avoid the cache, but that would change the method after the recorded runs.

