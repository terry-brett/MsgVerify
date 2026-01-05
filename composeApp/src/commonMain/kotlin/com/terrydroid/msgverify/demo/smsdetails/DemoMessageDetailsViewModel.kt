package com.terrydroid.msgverify.demo.smsdetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terrydroid.msgverify.data.MsgVerifyRepository
import com.terrydroid.msgverify.demo.smsoverview.Message
import com.terrydroid.msgverify.demo.smsoverview.getClassification
import com.terrydroid.msgverify.demo.smsoverview.getMessage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DemoMessageDetailsViewModel(
    private val msgVerifyRepository: MsgVerifyRepository,
    private val dispatcher: CoroutineDispatcher
) : ViewModel() {
    private val _state: MutableStateFlow<Message?> = MutableStateFlow(null)
    val state = _state.asStateFlow()

    fun init(id: Int) {
        viewModelScope.launch(dispatcher) {
            val message = getMessage(id)
            _state.value = message
            if (message != null) {
                msgVerifyRepository.verifyContent(message.message).collect { result ->
                    result.fold(
                        onSuccess = {
                            _state.value = message.copy(
                                trafficLight = getClassification(it.maliciousScore),
                                reason = it.description
                            )
                        },
                        onFailure = {
                            // No-OP
                        }
                    )
                }
            }
        }
    }
}


