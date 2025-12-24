package com.terrydroid.msgverify.demo.details

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun DemoMessageDetailsScreen(
    paddingValues: PaddingValues,
    id: Int,
    detailsViewModel: DemoMessageDetailsViewModel = koinViewModel()
) {
    val state = detailsViewModel.state.collectAsStateWithLifecycle().value

    LaunchedEffect("init") {
        detailsViewModel.init(id)
    }

    if (state != null) {
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                contentPadding = paddingValues,
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    Card {
                        Text(
                            text = state.message,
                            )
                    }
                }
            }
            ChatBox(modifier = Modifier.align(Alignment.BottomCenter))
        }
    } else {
        LinearProgressIndicator()
    }
}

@Composable
private fun ChatBox(modifier: Modifier) {
    var chatBoxValue by remember { mutableStateOf(TextFieldValue("")) }
    Row(modifier = modifier) {
        TextField(
            value = chatBoxValue,
            onValueChange = { newText ->
                chatBoxValue = newText
            },
            placeholder = {
                Text(text = "Type something")
            }
        )
        IconButton(
            onClick = {
                // TODO: send the message
            }
        ) {}
    }
}
