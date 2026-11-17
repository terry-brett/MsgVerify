package com.terrydroid.msgverify.benchmark

import com.terrydroid.msgverify.data.MsgVerifyRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.mp.KoinPlatform

/**
 * Entry point used by the platform launch hooks (MainActivity intent extras on Android,
 * process launch arguments on iOS). Starts at most one benchmark per process.
 * On failure a `benchmark-<runId>.failed` file is written so the launch scripts stop waiting.
 */
object BenchmarkLauncher {
  private var started = false
  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

  fun start(request: BenchmarkRequest) {
    if (started) return
    started = true
    println("MSGVERIFY_BENCHMARK_STARTED run=${request.runId} passes=${request.passes}")
    scope.launch {
      try {
        val verifier = KoinPlatform.getKoin().get<MsgVerifyRepository>().getContentVerifier()
        BenchmarkRunner(verifier).run(request)
      } catch (t: Throwable) {
        println("MSGVERIFY_BENCHMARK_FAILED ${t.stackTraceToString()}")
        writeBenchmarkFile("benchmark-${request.runId}.failed", t.stackTraceToString())
      }
    }
  }
}
