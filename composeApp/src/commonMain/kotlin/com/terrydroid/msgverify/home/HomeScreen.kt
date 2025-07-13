package com.terrydroid.msgverify.home

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.terrydroid.msgverify.home.components.LinkDescriptionBottomSheet
import com.terrydroid.msgverify.home.components.historicalLinkResults
import com.terrydroid.msgverify.home.components.inputField
import com.terrydroid.msgverify.home.components.linkHistoryHeader
import com.terrydroid.msgverify.home.components.linkVerificationSuccessField
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
            Spacer(modifier = Modifier.size(64.dp))
        }

        inputField(
            onVerifyClicked = onVerifyClicked,
            linkVerificationState = linkVerificationState
        )
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

               linkVerificationSuccessField(linkVerificationState)
            }
        }

       linkHistoryHeader(linkVerificationState)

       historicalLinkResults(
           linkVerificationState = linkVerificationState,
           onShowBottomSheet = onShowBottomSheet
       )
    }
}
