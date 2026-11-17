package com.terrydroid.msgverify

import android.os.Bundle
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.mutableStateOf
import com.terrydroid.msgverify.benchmark.BenchmarkLauncher
import com.terrydroid.msgverify.benchmark.BenchmarkRequest
import com.terrydroid.msgverify.benchmark.benchmarkAppContext

class MainActivity : ComponentActivity() {
    private val sharedTextState = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        handleIntent(intent)

        setContent {
            // Read .value directly in the composition to observe changes
            App(
                receivedText = sharedTextState.value,
                onTextConsumed = { sharedTextState.value = null }
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        // Benchmark mode, started by benchmark/run_android.sh:
        //   am start ... --ez msgverify_benchmark true --ei msgverify_passes N --es msgverify_run_id ID
        if (intent?.getBooleanExtra("msgverify_benchmark", false) == true) {
            benchmarkAppContext = applicationContext
            BenchmarkLauncher.start(
                BenchmarkRequest(
                    runId = intent.getStringExtra("msgverify_run_id") ?: "android",
                    passes = intent.getIntExtra("msgverify_passes", 10),
                    delayBetweenMessagesMs = intent.getLongExtra("msgverify_delay_ms", 0L),
                    startDelayMs = intent.getLongExtra("msgverify_start_delay_ms", 0L),
                )
            )
        }
        if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            val shared = intent.getStringExtra(Intent.EXTRA_TEXT)
            if (!shared.isNullOrBlank()) {
                sharedTextState.value = shared
            }
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
