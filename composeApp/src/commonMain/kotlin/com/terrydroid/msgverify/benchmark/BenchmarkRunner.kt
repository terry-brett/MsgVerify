package com.terrydroid.msgverify.benchmark

import kotlin.math.roundToLong
import kotlin.time.DurationUnit
import kotlin.time.TimeSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.contextguard.lib.ContentVerifier
import org.contextguard.models.TextClassificationResult

/** Parameters of a benchmark run, normally supplied by the launch scripts in `benchmark/`. */
data class BenchmarkRequest(
    val runId: String,
    val passes: Int = 10,
    /** Pause after each message, to reduce thermal build-up between inferences. */
    val delayBetweenMessagesMs: Long = 0,
    /** Wait before the baseline sample, e.g. to unplug a charging cable after launch. */
    val startDelayMs: Long = 0,
)

/**
 * Runs the full ContextGuard analysis ([ContentVerifier.verify]) over the benchmark corpus
 * [BenchmarkRequest.passes] times and writes one CSV row per inference.
 *
 * Timing covers exactly one `verify` call: URL extraction and classification, text
 * classification, heuristic labelling and the spellcheck. Memory is sampled after each call,
 * outside the timed region. No warm-up inferences are discarded and no garbage collection is
 * forced; the first inference of the process is marked so the analysis can treat it as cold start.
 */
class BenchmarkRunner(private val verifier: ContentVerifier) {

  suspend fun run(request: BenchmarkRequest): String = withContext(Dispatchers.IO) {
    if (request.startDelayMs > 0) {
      println("MSGVERIFY_BENCHMARK_WAITING ${request.startDelayMs} ms before starting")
      delay(request.startDelayMs)
    }
    val device = benchmarkDeviceInfo()
    val corpus = loadBenchmarkCorpus()
    val rows = mutableListOf<String>()
    rows += CSV_HEADER

    fun row(
        phase: String,
        pass: Int?,
        seq: Int?,
        message: BenchmarkMessage?,
        latencyMs: Double?,
        verdict: String?,
        maxUrlScore: Float?,
        urlCount: Int?,
        labelCount: Int?,
    ) {
      val mem = benchmarkMemorySnapshot()
      rows += listOf(
          request.runId, device.platform, device.osVersion, device.model,
          device.isSimulator, device.isDebugBuild, phase, pass, seq,
          message?.channel, message?.label, message?.id, message?.text?.length, urlCount,
          latencyMs?.let { (it * 1000).roundToLong() / 1000.0 }, verdict, maxUrlScore, labelCount,
          mem.footprintBytes, mem.residentBytes, mem.managedHeapBytes, mem.nativeHeapBytes,
          benchmarkThermalState(),
      ).joinToString(",") { csvField(it) }
    }

    // Memory before any model has been loaded.
    row("baseline", null, null, null, null, null, null, null, null)

    var seq = 0
    for (pass in 1..request.passes) {
      for (message in corpus) {
        seq++
        val start = TimeSource.Monotonic.markNow()
        val result = verifier.verify(content = message.text, sender = message.sender)
        val latencyMs = start.elapsedNow().toDouble(DurationUnit.MILLISECONDS)

        val verdict = when (result?.textClassificationResult) {
          is TextClassificationResult.Unsafe -> "unsafe"
          is TextClassificationResult.Safe -> "safe"
          null -> "error"
        }
        val labels = (result?.textClassificationResult as? TextClassificationResult.Unsafe)
            ?.listOfReasons?.size ?: 0
        row(
            phase = if (seq == 1) "cold_start" else "inference",
            pass = pass, seq = seq, message = message, latencyMs = latencyMs,
            verdict = verdict, maxUrlScore = result?.urlScores?.maxOrNull(),
            urlCount = result?.extractedUrls?.size ?: 0, labelCount = labels,
        )
        if (seq % 10 == 0) {
          // A stall here usually means the platform spellchecker is not responding.
          println("MSGVERIFY_BENCHMARK_PROGRESS $seq/${corpus.size * request.passes}")
        }
        if (request.delayBetweenMessagesMs > 0) delay(request.delayBetweenMessagesMs)
      }
    }

    val path = writeBenchmarkFile("benchmark-${request.runId}.csv", rows.joinToString("\n", postfix = "\n"))
    println("MSGVERIFY_BENCHMARK_DONE $path")
    path
  }

  private fun csvField(value: Any?): String {
    val text = value?.toString() ?: return ""
    return if (text.any { it == ',' || it == '"' || it == '\n' || it == '\r' }) {
      "\"" + text.replace("\"", "\"\"") + "\""
    } else text
  }

  companion object {
    val CSV_HEADER = listOf(
        "run_id", "platform", "os_version", "device_model", "is_simulator", "debug_build",
        "phase", "pass", "seq", "channel", "label", "message_id", "input_chars", "url_count",
        "latency_ms", "verdict", "max_url_score", "label_count",
        "mem_footprint_bytes", "mem_resident_bytes", "mem_managed_heap_bytes",
        "mem_native_heap_bytes", "thermal_state",
    ).joinToString(",")
  }
}
