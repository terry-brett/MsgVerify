package com.terrydroid.msgverify.testlab

import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.ParcelFileDescriptor
import android.os.SystemClock
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Runs the MsgVerify benchmark on a Firebase Test Lab device with the same steps as
 * benchmark/run_android.sh: for each run, force-stop the app, start it with the benchmark intent
 * extras and wait for `benchmark-<runId>.csv`. Each run therefore starts a new app process and has
 * one cold start. The device commands run as the shell user, like `adb shell` in the script.
 *
 * Finished CSVs, any `.failed` file and a conditions log per run are copied to [RESULTS_DIR],
 * which benchmark/testlab/run_testlab.sh pulls with `--directories-to-pull`.
 *
 * Instrumentation arguments (gcloud `--environment-variables`), with the protocol's defaults:
 * `runs` (3), `passes` (10), `startDelayS` (60), `runTimeoutS` (900), `runPrefix`
 * (`testlab-<device codename>`), and `waitForUnplugS` (0).
 *
 * On a local device, `waitForUnplugS` > 0 makes the test wait up to that many seconds for the USB
 * cable to be unplugged before the first run, so that all runs are measured on battery, as the
 * local protocol requires (benchmark/README.md). Start the test detached on the device, so that it
 * continues after the cable is unplugged:
 * `adb shell 'setsid nohup am instrument -w -e waitForUnplugS 900 ... > /data/local/tmp/log 2>&1 &'`.
 */
@RunWith(AndroidJUnit4::class)
class TestLabBenchmark {
  private val instrumentation = InstrumentationRegistry.getInstrumentation()
  private val args = InstrumentationRegistry.getArguments()

  @Test
  fun runBenchmark() {
    val runs = intArg("runs", 3)
    val passes = intArg("passes", 10)
    val startDelayS = intArg("startDelayS", 60)
    val runTimeoutS = intArg("runTimeoutS", 900)
    val prefix = args.getString("runPrefix") ?: "testlab-${Build.DEVICE.lowercase().safe()}"
    val waitForUnplugS = intArg("waitForUnplugS", 0)

    shell("rm -rf $RESULTS_DIR")
    shell("mkdir -p $RESULTS_DIR")
    if (waitForUnplugS > 0) waitForBattery(timeoutMs = waitForUnplugS * 1000L)
    for (run in 1..runs) {
      val runId = "$prefix-run$run"
      // The protocol keeps MsgVerify in the foreground with the screen on until the run ends.
      shell("svc power stayon true")
      shell("input keyevent KEYCODE_WAKEUP")
      shell("wm dismiss-keyguard")
      saveConditions(runId)
      shell("am force-stop $PKG")
      val started = shell(
          "am start -n $PKG/.MainActivity --ez msgverify_benchmark true --ei msgverify_passes $passes " +
              "--es msgverify_run_id $runId --el msgverify_delay_ms 0 " +
              "--el msgverify_start_delay_ms ${startDelayS * 1000L}")
      if ("Error" in started) fail("Could not start $PKG: $started")
      log("Run $run/$runs started: $runId (passes $passes, start delay ${startDelayS}s)")
      waitForResult(runId, timeoutMs = (startDelayS + runTimeoutS) * 1000L)
    }
    shell("am force-stop $PKG")
  }

  private fun waitForResult(runId: String, timeoutMs: Long) {
    val csv = "$APP_FILES/benchmark-$runId.csv"
    val failed = "$APP_FILES/benchmark-$runId.failed"
    val deadline = SystemClock.elapsedRealtime() + timeoutMs
    while (true) {
      SystemClock.sleep(POLL_MS)
      if (exists(csv)) {
        shell("cp $csv $RESULTS_DIR/")
        if (!exists("$RESULTS_DIR/benchmark-$runId.csv")) fail("Could not copy $csv to $RESULTS_DIR")
        log("Saved $RESULTS_DIR/benchmark-$runId.csv")
        return
      }
      if (exists(failed)) {
        shell("cp $failed $RESULTS_DIR/")
        fail("Benchmark $runId failed on the device:\n${shell("cat $failed")}")
      }
      if (SystemClock.elapsedRealtime() > deadline) {
        fail("Timed out waiting for $csv. If the logcat shows no new MSGVERIFY_BENCHMARK_PROGRESS " +
            "lines, the platform spellchecker is not responding (see benchmark/README.md).")
      }
    }
  }

  /** Waits until the device runs on battery (no charger, USB, wireless or dock power). */
  private fun waitForBattery(timeoutMs: Long) {
    log("Waiting up to ${timeoutMs / 1000}s for the cable to be unplugged")
    val deadline = SystemClock.elapsedRealtime() + timeoutMs
    while (pluggedType() != 0) {
      if (SystemClock.elapsedRealtime() > deadline) fail("The device was not unplugged within ${timeoutMs / 1000}s")
      SystemClock.sleep(1_000)
    }
    log("Running on battery")
  }

  /** BatteryManager.EXTRA_PLUGGED of the sticky battery broadcast: 0 when on battery. */
  private fun pluggedType(): Int =
      instrumentation.context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
          ?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: -1

  /** Records what the CSV does not: power, network and thermal state at the start of a run. */
  private fun saveConditions(runId: String) {
    val text = buildString {
      appendLine("run_id=$runId")
      appendLine("device=${Build.MANUFACTURER} ${Build.MODEL} (${Build.DEVICE}), Android ${Build.VERSION.RELEASE}")
      appendLine("fingerprint=${Build.FINGERPRINT}")
      appendLine("airplane_mode_on=${shell("settings get global airplane_mode_on").trim()}")
      appendLine("low_power=${shell("settings get global low_power").trim()}")
      appendLine("--- dumpsys battery")
      append(shell("dumpsys battery"))
      appendLine("--- dumpsys thermalservice (head)")
      appendLine(shell("dumpsys thermalservice").lineSequence().take(12).joinToString("\n"))
    }
    // The driver cannot write to the shell's directory directly, so it writes to its own files
    // directory and the shell copies the file.
    val local = java.io.File(instrumentation.context.getExternalFilesDir(null), "conditions-$runId.txt")
    local.writeText(text)
    shell("cp ${local.absolutePath} $RESULTS_DIR/")
  }

  private fun exists(path: String): Boolean = shell("ls $path").trim() == path

  /** Runs [command] as the shell user. No shell syntax: no pipes, redirects or quotes. */
  private fun shell(command: String): String {
    val output = instrumentation.uiAutomation.executeShellCommand(command)
    return ParcelFileDescriptor.AutoCloseInputStream(output).use { it.readBytes().decodeToString() }
  }

  private fun intArg(name: String, default: Int): Int =
      args.getString(name)?.toIntOrNull() ?: default

  private fun String.safe() = replace(Regex("[^a-z0-9-]"), "-")

  private fun log(message: String) {
    Log.i(TAG, message)
  }

  private companion object {
    const val TAG = "MsgVerifyTestLab"
    const val PKG = "com.terrydroid.msgverify"
    const val APP_FILES = "/sdcard/Android/data/$PKG/files"
    const val RESULTS_DIR = "/data/local/tmp/msgverify-benchmark"
    const val POLL_MS = 10_000L
  }
}
