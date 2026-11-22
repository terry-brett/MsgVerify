package com.terrydroid.msgverify

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.window.ComposeUIViewController
import com.terrydroid.msgverify.benchmark.BenchmarkLauncher
import com.terrydroid.msgverify.benchmark.BenchmarkRequest
import platform.Foundation.NSProcessInfo

private val sharedTextState = mutableStateOf<String?>(null)

fun MainViewController() = ComposeUIViewController {
    startBenchmarkIfRequested()
    App(
        receivedText = sharedTextState.value,
        onTextConsumed = { sharedTextState.value = null }
    )
}

fun setSharedText(text: String?) {
    sharedTextState.value = text
}

/**
 * Benchmark mode, started by benchmark/run_ios.sh, which launches the app with
 * `-msgverifyBenchmark -benchmarkPasses N -benchmarkRunId ID [-benchmarkDelayMs MS]`.
 */
private fun startBenchmarkIfRequested() {
    val args = NSProcessInfo.processInfo.arguments.map { it.toString() }
    if ("-msgverifyBenchmark" !in args) return
    fun value(flag: String): String? = args.indexOf(flag).takeIf { it >= 0 }?.let { args.getOrNull(it + 1) }
    BenchmarkLauncher.start(
        BenchmarkRequest(
            runId = value("-benchmarkRunId") ?: "ios",
            passes = value("-benchmarkPasses")?.toIntOrNull() ?: 10,
            delayBetweenMessagesMs = value("-benchmarkDelayMs")?.toLongOrNull() ?: 0L,
            startDelayMs = value("-benchmarkStartDelayMs")?.toLongOrNull() ?: 0L,
        )
    )
}

/**
 * Benchmark mode on Firebase Test Lab, started by iosApp/TestLabGameLoop.swift when Test Lab
 * opens the app with a Game Loop URL (see benchmark/testlab/README.md). Test Lab cannot pass
 * launch arguments, so this is the URL-based equivalent of [startBenchmarkIfRequested].
 */
fun startTestLabBenchmark(runId: String, passes: Int, startDelayMs: Long) {
    BenchmarkLauncher.start(BenchmarkRequest(runId = runId, passes = passes, startDelayMs = startDelayMs))
}
