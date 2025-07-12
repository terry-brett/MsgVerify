package com.terrydroid.msgverify

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.terrydroid.msgverify.di.commonModule
import com.terrydroid.msgverify.home.HomeScreen
import com.terrydroid.msgverify.home.HomeViewModel
import com.terrydroid.msgverify.theme.MsgVerifyTheme
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinApplication
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun App() {
    KoinApplication(application = {
        modules(commonModule())
    }) {
        AppScreen()
    }
}

@Composable
private fun AppScreen() {
    MsgVerifyTheme {
        Column(
            modifier = Modifier
                .background(color = MaterialTheme.colorScheme.background)
                .safeContentPadding()
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Scaffold(
                contentWindowInsets = WindowInsets(0.dp),
            ) { innerPadding ->
                HomeScreen(
                    paddingValues = innerPadding,
                )
            }
        }
    }
}

@Preview
@Composable
private fun AppPreview() {
    MsgVerifyTheme {
        AppScreen()
    }
}
