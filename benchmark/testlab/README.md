# Benchmark on Firebase Test Lab

Runs the benchmark in `benchmark/` on Firebase Test Lab physical devices, to measure devices that
are not available locally. The measured code and the protocol are the same as for
`run_android.sh` and `run_ios.sh`: the same app build, ContextGuard built from the source in this
repository (interpreter-reuse fix), the 90-message corpus, three runs of 10 passes per device, a
60 s wait after launch, and one new app process (one cold start) per run. The CSVs have the same
format, so `analyze.py` reads them unchanged.

Test Lab cannot run the two scripts, because they control the device from the host with `adb` and
`devicectl`. The files here do the same steps on the device.

| | Android | iOS |
|---|---|---|
| Test type | Instrumentation test, self-instrumenting (`android/`) | Game Loop (`iosApp/iosApp/TestLabGameLoop.swift`) |
| How a run starts | The test force-stops the app and starts it with the same intent extras as `run_android.sh` | Test Lab opens `firebase-game-loop://?scenario=N`; scenario N is run N |
| Between runs | `am force-stop`, then a new `am start` | The app writes the CSV, tells Test Lab the loop is complete, and exits |
| Results on the device | `/data/local/tmp/msgverify-benchmark/` | `Documents/GameLoopResults/` |
| App build | `benchmark` build type (release settings, not debuggable) | Release, minimum iOS lowered to 16.0 |

iOS uses a Game Loop and not an XCTest, because a UI test built with Xcode 17 or later needs
iOS 17 on the device, and most Test Lab iPhones run iOS 16. The `firebase-game-loop` URL scheme is
added to `Info.plist` only in the Test Lab build, so the Game Loop code never runs in a normal build.

## Differences from the local protocol

Test Lab devices stay connected to power and to the network, so airplane mode and battery-only
operation are not possible. Each run writes a `conditions-<run-id>.txt` file with the battery,
charging, low-power and thermal state at the start of the run, and the CSV records the thermal state
with every sample. Video recording and Test Lab's performance monitoring are off, because they use
the device's CPU during the run.

## Running

Requirements: the [gcloud CLI](https://cloud.google.com/sdk/docs/install) with the `beta` component
(`gcloud components install beta`), signed in (`gcloud auth login`), and a Firebase project on the
Blaze plan. The no-cost Spark plan allows five physical-device runs per day, so one failed device
means waiting a day. For iOS you also need Xcode, CocoaPods and an Apple Development signing
identity; Test Lab re-signs the app with its own certificate.

```bash
gcloud config set project <PROJECT_ID>
benchmark/testlab/run_testlab.sh --team <APPLE_TEAM_ID>
```

The script builds both platforms, starts the Android and iOS test matrices in parallel, waits for
them (about 10 minutes per device), and copies the CSVs and the conditions files to
`benchmark/results/testlab/`. Use `--android` or `--ios` for one platform, and `--skip-build` to
reuse the builds in `build/testlab/`. The devices are listed at the top of `run_testlab.sh`.

If the copy step fails, copy the files later from the results URL that the log shows:

```bash
benchmark/testlab/run_testlab.sh --fetch-only gs://<bucket>/<results-dir>
```

The build scripts can also run alone:

- `build_android.sh` writes `build/testlab/MsgVerify-benchmark.apk` and
  `MsgVerify-testlab-driver.apk`, and checks that the interpreter-reuse fix is in the APK.
- `build_ios.sh --team <ID>` writes `build/testlab/MsgVerify-testlab.ipa`.
- `build_ios.sh --simulator` builds an unsigned simulator app for a local check:

  ```bash
  xcrun simctl install booted build/testlab/MsgVerify-testlab-sim.app
  xcrun simctl launch booted com.terrydroid.msgverify \
    -testLabGameLoopURL "firebase-game-loop://?scenario=1&passes=1&startDelayS=0"
  ```

  The simulator asks for confirmation before it opens a custom URL with `simctl openurl`, so the
  check passes the URL as a launch argument, which goes to the same handler. The optional `passes`
  and `startDelayS` query items exist only for this check. Test Lab sends only `scenario`, so its
  runs always use the protocol values.
