package com.terrydroid.msgverify.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terrydroid.msgverify.data.MsgVerifyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val msgVerifyRepository: MsgVerifyRepository
) : ViewModel() {

    val safeBrowserState: StateFlow<SafeBrowserState>
        get() = _safeBrowserState.asStateFlow()

    private val _safeBrowserState: MutableStateFlow<SafeBrowserState> = MutableStateFlow(
        SafeBrowserState.Loading( false) // TODO : Get the initial state
    )

    fun toggleSafeBrowserState(toggle: Boolean) {
        viewModelScope.launch {
            if (toggle) { // TODO: Also check cache here I guess
                msgVerifyRepository.getSafeBrowserUrls().collect {
                    it.fold(
                        onSuccess = {
                            _safeBrowserState.value = SafeBrowserState.Success(enabled = true)
                        },
                        onFailure = {
                            _safeBrowserState.value =
                                SafeBrowserState.Error(
                                    message = "Could not get the urls",
                                    enabled = false
                                )
                        }
                    )
                }
            }
        }
    }
}

sealed interface SafeBrowserState {
    val enabled: Boolean

    data class Loading(override val enabled: Boolean) : SafeBrowserState
    data class Initial(override val enabled: Boolean) : SafeBrowserState
    data class Success(override val enabled: Boolean) : SafeBrowserState
    data class Error(val message: String, override val enabled: Boolean) : SafeBrowserState
}
