# Benchmark results

Raw CSVs from `run_android.sh` / `run_ios.sh`, one per run, as copied from the devices. New runs are written to this folder; the subfolders hold the recorded sets. Analyse one set at a time, because the sets were measured on different code:

```bash
.venv/bin/python benchmark/analyze.py benchmark/results/interpreter-fix/*.csv
```

| Folder | Code measured | Contents |
|---|---|---|
| `interpreter-fix/` | ContextGuard 1.0.3 source with the interpreter-reuse fix, built locally (`testLocally = true`) | Main result set: Galaxy Z Fold3, Pixel 9 Pro XL, Pixel 9a (GrapheneOS), Pixel Tablet, iPhone 12 Pro Max. Three runs of 10 passes each per device. The Pixel 9 Pro XL runs were started with the Test Lab driver (`../testlab/android/`) and `waitForUnplugS`, so that they ran on battery; `conditions-*.txt` records the device state at the start of each run. `excluded/` holds the OnePlus 6T runs, which are not part of the set: their footprint is a constant cached value (see the known issues in `../README.md`). |
| `testlab/` | Same build as `interpreter-fix/`, run on Firebase Test Lab with `../testlab/run_testlab.sh` | Test Lab physical devices: Galaxy A16 (Android 15), iPhone 8, iPhone 11 Pro and iPhone 14 Pro (iOS 16.6), iPhone 16 Pro (iOS 18.3.2). Three runs of 10 passes each per device. The devices stay on power and on the network; `conditions-*.txt` records battery, charging, low-power and thermal state at the start of each run. |
| `contextguard-1.0.3/` | Published `org.contextguard.lib:contextguard:1.0.3` | Earlier Android runs (Fold3, Pixel 9a), before the iOS memory leak was found. `superseded/` holds one run replaced by a cleaner repeat. |
| `diagnostic/` | Both versions | Single-pass iPhone runs used to find and verify the leak: `diag` (1.0.3, memory grows about 5 MB per message), `fixtest` (closing the interpreter, no effect because `close()` is a no-op on iOS in ktensorflow 0.2), `fixtest2` (interpreter reuse, memory levels off). `run1.failed` is a one-off interpreter creation failure on the first iOS launch that did not recur. `privacy-online` and `privacy-online2` are single passes run with the network available while the iOS App Privacy Report was recording, as evidence that MsgVerify makes no network connections. `pixel9proxl-diag-spellon` and `pixel9proxl-diag-spelloff` are two-pass runs on the Pixel 9 Pro XL, with the cable connected and the network on, first with the platform spell checker on and then with it switched off (`settings put secure spell_checker_enabled 0`). They show that the spell checker (Gboard 18.8.0 beta) took most of that device's analysis time: median 40.2 ms with it, 7.0 ms without. |

Conditions that the CSVs do not record (battery, charging, airplane mode) were logged per run alongside the paper's copy of this data.
