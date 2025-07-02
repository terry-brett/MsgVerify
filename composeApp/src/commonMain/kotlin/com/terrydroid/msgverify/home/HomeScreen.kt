package com.terrydroid.msgverify.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun HomeScreen(
    homeViewModel: HomeViewModel = koinViewModel(),
    paddingValues: PaddingValues
) {
    val linkVerificationState =
        homeViewModel
            .linkVerificationState
            .collectAsStateWithLifecycle()
            .value

    Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = "MsgVerify",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.size(64.dp))
        val textFieldValue = remember { mutableStateOf("") }
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(80f),
            value = textFieldValue.value,
            onValueChange = {
                textFieldValue.value = it
            },
            placeholder = {
                Text(text = "Enter Url you want to verify")
            },
            isError = linkVerificationState is LinkVerificationState.Error,
            label = {
                if (linkVerificationState is LinkVerificationState.Error) {
                    Text(linkVerificationState.errorMessage)
                }
            }
        )

        Button(
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 16.dp),
            onClick = {
                homeViewModel.onVerifyClicked(textFieldValue.value)
            },
            enabled = textFieldValue.value.isNotEmpty()
        ) {
            Text(text = "Verify")
        }

        when (linkVerificationState) {
            is LinkVerificationState.Error -> {
                Text(linkVerificationState.errorMessage)
            }

            LinkVerificationState.Idle -> {
                /* NO-OP */
            }

            LinkVerificationState.LoadingVerification -> {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = "Processing link",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            is LinkVerificationState.Success -> {
                Spacer(Modifier.size(24.dp))
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = "There is a ${linkVerificationState.linkMaliciousPercentage} % " +
                            "chance its malicious",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}
