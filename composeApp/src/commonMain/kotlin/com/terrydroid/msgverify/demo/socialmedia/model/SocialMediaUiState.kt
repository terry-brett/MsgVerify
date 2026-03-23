package com.terrydroid.msgverify.demo.socialmedia.model

import com.terrydroid.msgverify.demo.socialmedia.Message

sealed interface SocialMediaUiState {
    data object Loading : SocialMediaUiState
    data class Success(val message: List<Message>) : SocialMediaUiState
}
