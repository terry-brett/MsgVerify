#!/usr/bin/env python3
"""Analyse MsgVerify benchmark CSVs produced by run_android.sh / run_ios.sh.

Usage:
    python benchmark/analyze.py [CSV ...] [--out benchmark/results/summary]
                                [--include-simulators] [--include-debug]

With no CSV arguments, every benchmark-*.csv in benchmark/results/ is read.

Outputs (in --out):
    latency_by_device.csv per device and channel: steady-state latency, pooled over runs
    latency_by_run.csv    the same per run
    cold_start.csv        first inference of each run
    paired_tests.csv      each iOS device vs each Android device, paired by message
    memory_summary.csv    per run: baseline, post-load and peak memory
    summary.md            the tables above in Markdown
    latency.pdf           latency distributions per channel and platform
    memory.pdf            memory footprint across each run, one panel per device

Method notes:
    * Rows from simulators/emulators and debuggable builds are excluded unless the
      corresponding --include flag is given, because their timings do not reflect devices.
    * "Steady state" excludes the single cold-start row (first inference of the process).
    * The platform comparison pairs each message with itself: for every message the
      median latency over all passes is taken per platform, and the two medians are
      compared with a two-sided Wilcoxon signed-rank test, for every iOS/Android device pair
      and channel. Effect size is the matched-pairs rank-biserial correlation. p-values are
      Holm-Bonferroni adjusted across all device pairs and channels.
"""
from __future__ import annotations

import argparse
import glob
import os
import sys

import matplotlib

matplotlib.use("Agg")
import matplotlib.pyplot as plt  # noqa: E402
import numpy as np  # noqa: E402
import pandas as pd  # noqa: E402
from scipy import stats  # noqa: E402

CHANNELS = ["sms", "email", "social"]
CHANNEL_NAMES = {"sms": "SMS", "email": "Email", "social": "Social media"}
PLATFORM_NAMES = {"android": "Android", "ios": "iOS"}
# Same blue/orange as the paper's diagrams; validated for colour-vision deficiency.
# Marketing names for the model identifiers the devices report. Unknown models keep their ID.
DEVICE_NAMES = {
    "samsung SM-F926B": "Galaxy Z Fold3",
    "Google Pixel 9a": "Pixel 9a",
    "Google Pixel Tablet": "Pixel Tablet",
    "OnePlus ONEPLUS A6013": "OnePlus 6T",
    "iPhone13,4": "iPhone 12 Pro Max",
}
PLATFORM_COLOURS = {"android": "#2B6CB0", "ios": "#D9822B"}
INK, MUTED, GRID = "#1A202C", "#4A5568", "#E2E8F0"
MB = 1024 * 1024


def load(paths: list[str], include_simulators: bool, include_debug: bool) -> pd.DataFrame:
    frames = [pd.read_csv(p) for p in paths]
    df = pd.concat(frames, ignore_index=True)
    for col in ("is_simulator", "debug_build"):
        df[col] = df[col].astype(str).str.lower().eq("true")
    dropped = []
    if not include_simulators and df["is_simulator"].any():
        dropped.append(f"{df['is_simulator'].sum()} simulator/emulator rows")
        df = df[~df["is_simulator"]]
    if not include_debug and df["debug_build"].any():
        dropped.append(f"{df['debug_build'].sum()} debug-build rows")
        df = df[~df["debug_build"]]
    for note in dropped:
        print(f"Excluded {note}.", file=sys.stderr)
    if df.empty:
        sys.exit("No rows left to analyse.")
    names = df["device_model"].map(lambda m: DEVICE_NAMES.get(m, m))
    df["device"] = df["platform"].map(PLATFORM_NAMES) + " (" + names + ")"
    # A footprint that never changes during a run is a cached value, not a measurement (seen with
    # Debug.getMemoryInfo on Android 11). Drop it for that run so it is not reported as real.
    for run_id, run in df.groupby("run_id"):
        if run["mem_footprint_bytes"].notna().any() and run["mem_footprint_bytes"].nunique() == 1:
            print(f"Warning: {run_id}: footprint is constant across the run (cached by the OS); "
                  "excluded from the memory results.", file=sys.stderr)
            df.loc[df["run_id"] == run_id, "mem_footprint_bytes"] = np.nan
    return df


def latency_summary(df: pd.DataFrame, by_run: bool) -> pd.DataFrame:
    steady = df[df["phase"] == "inference"]
    keys = (["run_id"] if by_run else []) + ["device", "channel"]
    g = steady.groupby(keys)["latency_ms"]
    out = pd.DataFrame({
        "n": g.size(),
        "median_ms": g.median(),
        "q1_ms": g.quantile(0.25),
        "q3_ms": g.quantile(0.75),
        "p95_ms": g.quantile(0.95),
        "mean_ms": g.mean(),
        "sd_ms": g.std(),
        "max_ms": g.max(),
        "pct_under_100ms": g.apply(lambda s: 100.0 * (s < 100).mean()),
    }).reset_index()
    return out.round(2)


def cold_starts(df: pd.DataFrame) -> pd.DataFrame:
    cols = ["run_id", "device", "channel", "message_id", "latency_ms"]
    return df[df["phase"] == "cold_start"][cols].round(2)


def holm(pvalues: list[float]) -> list[float]:
    order = np.argsort(pvalues)
    m = len(pvalues)
    adjusted = np.empty(m)
    running = 0.0
    for rank, idx in enumerate(order):
        running = max(running, min(1.0, (m - rank) * pvalues[idx]))
        adjusted[idx] = running
    return adjusted.tolist()


def paired_tests(df: pd.DataFrame) -> pd.DataFrame | None:
    """Each iOS device against each Android device, per channel, paired by message."""
    steady = df[df["phase"] == "inference"]
    android = sorted(steady.loc[steady["platform"] == "android", "device"].unique())
    ios = sorted(steady.loc[steady["platform"] == "ios", "device"].unique())
    if not android or not ios:
        print("Paired tests skipped: need steady-state data from both Android and iOS.", file=sys.stderr)
        return None
    # Median over all passes and runs, per message and device.
    per_msg = steady.groupby(["device", "channel", "message_id"])["latency_ms"].median()
    rows = []
    for a in android:
        for i in ios:
            for channel in CHANNELS:
                try:
                    pair = pd.concat({"a": per_msg.loc[a, channel], "i": per_msg.loc[i, channel]},
                                     axis=1).dropna()
                except KeyError:
                    continue
                if len(pair) < 6:
                    continue
                diff = pair["a"] - pair["i"]
                nonzero = diff[diff != 0]
                res = stats.wilcoxon(pair["a"], pair["i"], alternative="two-sided")
                ranks = stats.rankdata(nonzero.abs())
                w_plus, w_minus = ranks[nonzero > 0].sum(), ranks[nonzero < 0].sum()
                rows.append({
                    "android_device": a,
                    "ios_device": i,
                    "channel": channel,
                    "n_pairs": len(pair),
                    "median_android_ms": pair["a"].median(),
                    "median_ios_ms": pair["i"].median(),
                    "median_diff_ms": diff.median(),
                    "wilcoxon_W": res.statistic,
                    "p_raw": res.pvalue,
                    "rank_biserial_r": (w_plus - w_minus) / (w_plus + w_minus) if len(nonzero) else 0.0,
                })
    if not rows:
        return None
    out = pd.DataFrame(rows)
    out["p_holm"] = holm(out["p_raw"].tolist())  # across all device pairs and channels
    # Round everything except the p-values, which can be very small.
    other = [c for c in out.columns if c not in ("p_raw", "p_holm")]
    out[other] = out[other].round(4)
    return out


def memory_summary(df: pd.DataFrame) -> pd.DataFrame:
    rows = []
    for (run_id, device), run in df.groupby(["run_id", "device"]):
        base = run[run["phase"] == "baseline"]
        inf = run[run["phase"] != "baseline"].sort_values("seq")
        if base.empty or inf.empty:
            continue
        row = {"run_id": run_id, "device": device}
        for col, name in [("mem_footprint_bytes", "footprint"), ("mem_resident_bytes", "resident"),
                          ("mem_managed_heap_bytes", "managed_heap"),
                          ("mem_native_heap_bytes", "native_heap")]:
            if inf[col].notna().any():
                b = base[col].iloc[0] / MB
                row[f"{name}_baseline_mb"] = b
                row[f"{name}_after_first_mb"] = inf[col].iloc[0] / MB
                row[f"{name}_median_mb"] = inf[col].median() / MB
                row[f"{name}_peak_mb"] = inf[col].max() / MB
                row[f"{name}_peak_increase_mb"] = inf[col].max() / MB - b
        row["thermal_states"] = ";".join(f"{k}:{v}" for k, v in
                                          inf["thermal_state"].value_counts().items())
        rows.append(row)
    return pd.DataFrame(rows).round(1)


def style_axes(ax):
    ax.spines[["top", "right"]].set_visible(False)
    for side in ("left", "bottom"):
        ax.spines[side].set_color(MUTED)
    ax.tick_params(colors=MUTED, labelcolor=INK, labelsize=8)
    ax.yaxis.grid(True, color=GRID, linewidth=0.6)
    ax.set_axisbelow(True)


def device_order(df: pd.DataFrame) -> list[str]:
    devices = df[["platform", "device"]].drop_duplicates().sort_values(["platform", "device"])
    return devices["device"].tolist()


def short_label(device: str) -> str:
    # "Android (samsung SM-F926B)" -> "SM-F926B\n(Android)"
    platform, _, model = device.partition(" (")
    return f"{model.rstrip(')')}\n({platform})"


def plot_latency(df: pd.DataFrame, path: str):
    """Horizontal boxplots: one row per device, one column per channel, shared latency axis."""
    steady = df[df["phase"] == "inference"]
    devices = device_order(steady)[::-1]  # first device at the top
    platform_of = dict(steady[["device", "platform"]].drop_duplicates().values)
    fig, axes = plt.subplots(1, len(CHANNELS), figsize=(6.3, 0.42 * len(devices) + 1.2),
                             sharex=True, sharey=True)
    for ax, channel in zip(axes, CHANNELS):
        for y, device in enumerate(devices):
            colour = PLATFORM_COLOURS.get(platform_of[device], MUTED)
            data = steady[(steady["device"] == device) & (steady["channel"] == channel)]["latency_ms"]
            if data.empty:
                continue
            ax.boxplot([data], positions=[y], widths=0.55, vert=False, patch_artist=True,
                       boxprops=dict(facecolor=colour, edgecolor=colour, alpha=0.35, linewidth=1),
                       medianprops=dict(color=colour, linewidth=2),
                       whiskerprops=dict(color=colour, linewidth=1),
                       capprops=dict(color=colour, linewidth=1),
                       flierprops=dict(marker="o", markersize=3, markerfacecolor=colour,
                                       markeredgecolor="none", alpha=0.6))
        ax.set_title(CHANNEL_NAMES[channel], fontsize=9, color=INK)
        ax.set_xlabel("Latency (ms)", fontsize=8, color=INK)
        style_axes(ax)
        ax.yaxis.grid(False)
        ax.xaxis.grid(True, color=GRID, linewidth=0.6)
    axes[0].set_yticks(range(len(devices)), [short_label(d).replace("\n", " ") for d in devices],
                       fontsize=7.5)
    axes[0].set_ylim(-0.6, len(devices) - 0.4)
    axes[0].set_xlim(left=0)
    fig.tight_layout()
    fig.savefig(path)
    plt.close(fig)


def plot_memory(df: pd.DataFrame, path: str):
    df = df[df.groupby("device")["mem_footprint_bytes"].transform(lambda s: s.notna().any())]
    devices = device_order(df)
    cols = min(3, len(devices))
    rows = -(-len(devices) // cols)
    fig, axes = plt.subplots(rows, cols, figsize=(6.3, 2.3 * rows), sharey=True, squeeze=False)
    for ax in axes.flat[len(devices):]:
        ax.set_visible(False)
    for ax, device in zip(axes.flat, devices):
        dev_df = df[df["device"] == device]
        colour = PLATFORM_COLOURS.get(dev_df["platform"].iloc[0], MUTED)
        for _, run in dev_df.groupby("run_id"):
            base = run[run["phase"] == "baseline"]["mem_footprint_bytes"]
            inf = run[run["phase"] != "baseline"].sort_values("seq")
            ax.plot(inf["seq"], inf["mem_footprint_bytes"] / MB, color=colour, linewidth=1.2)
            if not base.empty:
                ax.axhline(base.iloc[0] / MB, color="#A0AEC0", linewidth=1)
        ax.set_title(short_label(device).replace("\n", " "), fontsize=8, color=INK)
        style_axes(ax)
    # Label the x axis on the lowest visible panel of each column.
    for col in range(cols):
        visible = [r for r in range(rows) if r * cols + col < len(devices)]
        if visible:
            axes[visible[-1]][col].set_xlabel("Inference number", fontsize=8, color=INK)
    for ax in axes[:, 0]:
        ax.set_ylabel("Footprint (MB)", fontsize=8, color=INK)
    axes[0][0].set_ylim(bottom=0)
    fig.text(0.99, 0.005, "Footprint: total PSS (Android), phys_footprint (iOS). "
             "Grey line: before first inference. One line per run.",
             ha="right", va="bottom", fontsize=6.5, color=MUTED)
    fig.tight_layout(rect=(0, 0.03, 1, 1))
    fig.savefig(path)
    plt.close(fig)


def main():
    here = os.path.dirname(os.path.abspath(__file__))
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("csv", nargs="*")
    parser.add_argument("--out", default=os.path.join(here, "results", "summary"))
    parser.add_argument("--include-simulators", action="store_true")
    parser.add_argument("--include-debug", action="store_true")
    args = parser.parse_args()

    paths = args.csv or sorted(glob.glob(os.path.join(here, "results", "benchmark-*.csv")))
    if not paths:
        sys.exit("No benchmark CSVs found.")
    os.makedirs(args.out, exist_ok=True)
    plt.rcParams.update({"pdf.fonttype": 42, "font.family": "sans-serif"})

    df = load(paths, args.include_simulators, args.include_debug)
    tables = {
        "latency_by_device": latency_summary(df, by_run=False),
        "latency_by_run": latency_summary(df, by_run=True),
        "cold_start": cold_starts(df),
        "paired_tests": paired_tests(df),
        "memory_summary": memory_summary(df),
    }
    with open(os.path.join(args.out, "summary.md"), "w") as md:
        md.write(f"# MsgVerify benchmark summary\n\nInputs: {', '.join(os.path.basename(p) for p in paths)}\n")
        for name, table in tables.items():
            if table is None or table.empty:
                continue
            table.to_csv(os.path.join(args.out, f"{name}.csv"), index=False)
            md.write(f"\n## {name.replace('_', ' ').capitalize()}\n\n{table.to_markdown(index=False)}\n")
    plot_latency(df, os.path.join(args.out, "latency.pdf"))
    plot_memory(df, os.path.join(args.out, "memory.pdf"))
    print(f"Wrote results to {args.out}")


if __name__ == "__main__":
    main()
