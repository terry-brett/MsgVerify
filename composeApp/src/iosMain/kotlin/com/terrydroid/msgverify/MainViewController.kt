package com.terrydroid.msgverify

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.window.ComposeUIViewController

private val sharedTextState = mutableStateOf<String?>(null)

fun MainViewController() = ComposeUIViewController {
    App(
        recievedText = sharedTextState.value,
        onTextConsumed = { sharedTextState.value = null }
    )
}

fun setSharedText(text: String?) {
    sharedTextState.value = text
}