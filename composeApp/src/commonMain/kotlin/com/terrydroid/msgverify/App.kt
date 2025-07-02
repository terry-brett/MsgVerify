package com.terrydroid.msgverify

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.terrydroid.msgverify.di.commonModule
import com.terrydroid.msgverify.home.HomeViewModel
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
    MaterialTheme {
        Column(
            modifier = Modifier
                .safeContentPadding()
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val vm: HomeViewModel = koinViewModel()

            Scaffold(
                contentWindowInsets = WindowInsets(0.dp)
            ) { innerPadding ->
                Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                    Spacer(modifier = Modifier.size(160.dp))
                    val textFieldValue = remember { mutableStateOf("") }
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(80f),
                        value = textFieldValue.value,
                        onValueChange = {
                            textFieldValue.value = it
                        },
                        placeholder = {
                            Text("Enter Url you want to verify")
                        }
                    )

                    Button(
                        onClick = {
                            vm.onVerifyClicked(textFieldValue.value)
                        }
                    ) {
                        Text("Verify")
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
        AppScreen()
    }
}
