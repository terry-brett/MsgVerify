# Benchmark results

Raw CSVs from `run_android.sh` / `run_ios.sh`, one per run, as copied from the devices. New runs are written to this folder; the subfolders hold the recorded sets. Analyse one set at a time, because the sets were measured on different code:

```bash
.venv/bin/python benchmark/analyze.py benchmark/results/interpreter-fix/*.csv
```

| Folder | Code measured | Contents |
|---|---|---|
| `interpreter-fix/` | ContextGuard 1.0.3 source with the interpreter-reuse fix, built locally (`testLocally = true`) | Main result set: Galaxy Z Fold3, Pixel 9a (GrapheneOS), Pixel Tablet, iPhone 12 Pro Max. Three runs of 10 passes each per device. |
| `contextguard-1.0.3/` | Published `org.contextguard.lib:contextguard:1.0.3` | Earlier Android runs (Fold3, Pixel 9a), before the iOS memory leak was found. `superseded/` holds one run replaced by a cleaner repeat. |
| `diagnostic/` | Both versions | Single-pass iPhone runs used to find and verify the leak: `diag` (1.0.3, memory grows about 5 MB per message), `fixtest` (closing the interpreter, no effect because `close()` is a no-op on iOS in ktensorflow 0.2), `fixtest2` (interpreter reuse, memory levels off). `run1.failed` is a one-off interpreter creation failure on the first iOS launch that did not recur. |

Conditions that the CSVs do not record (battery, charging, airplane mode) were logged per run alongside the paper's copy of this data.
