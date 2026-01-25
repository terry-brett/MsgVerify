package com.terrydroid.msgverify.demo.smsdetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terrydroid.msgverify.data.ContentVerificationResponse
import com.terrydroid.msgverify.data.MsgVerifyRepository
import com.terrydroid.msgverify.demo.smsoverview.Message
import com.terrydroid.msgverify.demo.smsoverview.TrafficLight
import com.terrydroid.msgverify.demo.smsoverview.UrlScore
import com.terrydroid.msgverify.demo.smsoverview.getMessage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DemoMessageDetailsViewModel(
    private val msgVerifyRepository: MsgVerifyRepository,
    private val dispatcher: CoroutineDispatcher,
) : ViewModel() {
    private val _state: MutableStateFlow<Message?> = MutableStateFlow(null)
    val state = _state.asStateFlow()
    private var currentJob: Job? = null

    fun init(id: Int) {
        // Cancel previous job if any
        currentJob?.cancel()

        currentJob = viewModelScope.launch(dispatcher) {
            val message = getMessage(id)
            _state.value = message
            if (message != null) {
                // Switch to main thread for verification that accesses Android UI APIs
                withContext(viewModelScope.coroutineContext) {
                    msgVerifyRepository.verifyContent(message.message, message.title).collect { result ->
                        result.fold(
                            onSuccess = { response ->
                                val classification: TrafficLight
                                val reasons: List<String>
                                val urlScores: List<UrlScore>

                                when (response) {
                                    is ContentVerificationResponse.Safe -> {
                                        classification = TrafficLight.Green
                                        reasons = emptyList()
                                        urlScores = emptyList()
                                    }
                                    is ContentVerificationResponse.Unsafe -> {
                                        classification = TrafficLight.Red
                                        reasons = response.reasons.map { it.reason }
                                        urlScores = (response.extractedUrls ?: emptyList())
                                            .zip(response.urlScores ?: emptyList())
                                            .map { (url, score) -> UrlScore(url, score) }
                                    }
                                }

                                _state.value = message.copy(
                                    trafficLight = classification,
                                    reasons = reasons,
                                    urlScores = urlScores
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
}


