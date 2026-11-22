# Benchmark results

Raw CSVs from `run_android.sh` / `run_ios.sh`, one per run, as copied from the devices. New runs are written to this folder; the subfolders hold the recorded sets. Analyse one set at a time, because the sets were measured on different code:

```bash
.venv/bin/python benchmark/analyze.py benchmark/results/interpreter-fix/*.csv
```

| Folder | Code measured | Contents |
|---|---|---|
| `interpreter-fix/` | ContextGuard 1.0.3 source with the interpreter-reuse fix, built locally (`testLocally = true`) | Main result set: Galaxy Z Fold3, OnePlus 6T, Pixel 9a (GrapheneOS), Pixel Tablet, iPhone 12 Pro Max. Three runs of 10 passes each per device. The OnePlus 6T footprint is a constant cached value (see the known issues in `../README.md`), and the analysis excludes it. |
| `testlab/` | Same build as `interpreter-fix/`, run on Firebase Test Lab with `../testlab/run_testlab.sh` | Test Lab physical devices: iPhone 8 (iOS 16.6). Three runs of 10 passes each per device. The devices stay on power and on the network; `conditions-*.txt` records battery, charging, low-power and thermal state at the start of each run. |
| `contextguard-1.0.3/` | Published `org.contextguard.lib:contextguard:1.0.3` | Earlier Android runs (Fold3, Pixel 9a), before the iOS memory leak was found. `superseded/` holds one run replaced by a cleaner repeat. |
| `diagnostic/` | Both versions | Single-pass iPhone runs used to find and verify the leak: `diag` (1.0.3, memory grows about 5 MB per message), `fixtest` (closing the interpreter, no effect because `close()` is a no-op on iOS in ktensorflow 0.2), `fixtest2` (interpreter reuse, memory levels off). `run1.failed` is a one-off interpreter creation failure on the first iOS launch that did not recur. `privacy-online` and `privacy-online2` are single passes run with the network available while the iOS App Privacy Report was recording, as evidence that MsgVerify makes no network connections. |

Conditions that the CSVs do not record (battery, charging, airplane mode) were logged per run alongside the paper's copy of this data.
