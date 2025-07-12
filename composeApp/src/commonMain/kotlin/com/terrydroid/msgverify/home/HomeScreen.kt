package com.terrydroid.msgverify.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.terrydroid.msgverify.home.components.LinkDescriptionBottomSheet
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
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
    HomeScreen(
        linkVerificationState = linkVerificationState,
        paddingValues = paddingValues,
        onHideBottomSheet = homeViewModel::onHideBottomSheet,
        onShowBottomSheet = homeViewModel::onShowBottomSheetDescription,
        onVerifyClicked = homeViewModel::onVerifyClicked
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreen(
    linkVerificationState: LinkVerificationState,
    paddingValues: PaddingValues,
    onHideBottomSheet: () -> Unit,
    onShowBottomSheet: (linkResult: LinkResult) -> Unit,
    onVerifyClicked: (link: String) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()

    LaunchedEffect(linkVerificationState) {
        sheetState.currentValue
    }
    if (linkVerificationState.bottomSheetInformation != null) {
        LinkDescriptionBottomSheet(
            description = linkVerificationState.bottomSheetInformation?.description ?: "",
            onHideBottomSheet = onHideBottomSheet,
            sheetState = sheetState
        )
    }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
        item {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "MsgVerify",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
        }
        item {
            Spacer(modifier = Modifier.size(64.dp))
        }

        item {
            val textFieldValue = remember { mutableStateOf("") }
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(70f),
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
                    } else {
                        Text("Input a url to verify")
                    }
                },
                maxLines = 1,
                shape = RoundedCornerShape(8.dp),
                trailingIcon = {
                    Icon(imageVector = Icons.Default.Policy, contentDescription = null)
                }
            )

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                onClick = {
                   onVerifyClicked.invoke(textFieldValue.value)
                },
                enabled = textFieldValue.value.isNotEmpty()
            ) {
                Text(
                    modifier = Modifier.padding(8.dp),
                    text = "Verify"
                )
            }
        }

        item {
            Spacer(modifier = Modifier.size(16.dp))
        }

        when (linkVerificationState) {
            is LinkVerificationState.Error -> {
                /* Handled in the text field */
            }

            is LinkVerificationState.Idle -> {
                /* NO-OP */
            }

            is LinkVerificationState.LoadingVerification -> {
                item {
                    CircularProgressIndicator()
                }
            }

            is LinkVerificationState.Success -> {

                item {
                    //TODO: Implement warning and success colors
                    val textColor = when (linkVerificationState.linkResult.classificationColor) {
                        ClassificationColor.Red -> MaterialTheme.colorScheme.error
                        ClassificationColor.Yellow -> MaterialTheme.colorScheme.onBackground
                        ClassificationColor.Green -> MaterialTheme.colorScheme.onBackground
                    }
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = "There is a ${linkVerificationState.linkResult.linkMaliciousPercentage} % " +
                                "chance it's malicious",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelMedium,
                        color = textColor
                    )
                }
            }
        }

        item {
            if (linkVerificationState.verifiedLinkHistory.isNotEmpty()) {
                Text(
                    modifier = Modifier.padding(top = 24.dp),
                    text = "Previously verified links",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        items(linkVerificationState.verifiedLinkHistory) { item ->
            Row(
                modifier = Modifier.fillMaxWidth().clickable(onClick = {
                   onShowBottomSheet.invoke(item)
                }),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = item.url,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.size(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val icon = when (item.classificationColor) {
                        ClassificationColor.Red -> Icons.Default.Error
                        ClassificationColor.Yellow -> Icons.Default.Warning
                        ClassificationColor.Green -> Icons.Default.Shield
                    }
                    Icon(
                        modifier = Modifier.padding(4.dp),
                        imageVector = icon,
                        contentDescription = null
                    )
                    Text(
                        text = "${item.linkMaliciousPercentage} %",
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
    }
}
