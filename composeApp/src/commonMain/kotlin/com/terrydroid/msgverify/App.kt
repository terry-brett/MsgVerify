package com.terrydroid.msgverify

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.terrydroid.msgverify.di.commonModule
import com.terrydroid.msgverify.home.HomeViewModel
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinApplication
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.KoinApplication

@Composable
fun App() {
    KoinApplication(application = {
        modules(commonModule())
    }) {
        MaterialTheme {
            Column(
                modifier = Modifier
                    .safeContentPadding()
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                val vm : HomeViewModel = koinViewModel()

                Scaffold(
                    contentWindowInsets = WindowInsets(0.dp)
                ) { innerPadding ->
                    Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {

                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun AppPreview() {
    MaterialTheme {
        App()
    }
}
