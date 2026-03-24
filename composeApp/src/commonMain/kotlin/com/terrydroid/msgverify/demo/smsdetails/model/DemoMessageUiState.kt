package com.terrydroid.msgverify.demo.smsdetails.model

import com.terrydroid.msgverify.demo.smsoverview.Message

sealed interface DemoMessageUiState {
    data object Loading : DemoMessageUiState
    data object Error : DemoMessageUiState
    data class Success(val message: Message): DemoMessageUiState
}