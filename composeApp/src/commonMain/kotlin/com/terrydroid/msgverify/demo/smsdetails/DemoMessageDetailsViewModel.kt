package com.terrydroid.msgverify.demo.smsdetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terrydroid.msgverify.data.MsgVerifyRepository
import com.terrydroid.msgverify.demo.mapper.toUiModels
import com.terrydroid.msgverify.demo.smsdetails.model.DemoMessageUiState
import com.terrydroid.msgverify.demo.smsoverview.getMessage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DemoMessageDetailsViewModel(
    private val msgVerifyRepository: MsgVerifyRepository,
    private val dispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val _uiState = MutableStateFlow<DemoMessageUiState>(DemoMessageUiState.Loading)
    val uiState = _uiState.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        DemoMessageUiState.Loading
    )


    fun init(id: Int) {
        viewModelScope.launch(dispatcher) {
            _uiState.value = DemoMessageUiState.Loading

            val message = getMessage(id) ?: return@launch // Add proper error state here

            msgVerifyRepository.verifyContent(message.message, message.title).collect { result ->
                val updatedMessage = result.fold(
                    onSuccess = { response ->
                        val (light, reasons, scores) = response.toUiModels()
                        message.copy(trafficLight = light, reasons = reasons, urlScores = scores)
                    },
                    onFailure = { message }
                )
                _uiState.value = DemoMessageUiState.Success(updatedMessage)
            }
        }
    }
}
