package com.terrydroid.msgverify.demo.smsdetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terrydroid.msgverify.data.ContentVerificationResponse
import com.terrydroid.msgverify.data.MsgVerifyRepository
import com.terrydroid.msgverify.demo.smsoverview.Message
import com.terrydroid.msgverify.demo.smsoverview.TrafficLight
import com.terrydroid.msgverify.demo.smsoverview.UrlScore
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
                msgVerifyRepository.verifyContent(message.message, message.title).collect { result ->
                    result.fold(
                        onSuccess = { response ->
                            val classification: TrafficLight
                            val reasons: List<String>
                            val urlScores: List<UrlScore>

                            when (response) {
                                is ContentVerificationResponse.Safe -> {
                                    classification = getClassification(0f)
                                    reasons = emptyList()
                                    urlScores = emptyList()
                                }
                                is ContentVerificationResponse.Unsafe -> {
                                    val maxScore = response.urlScores?.maxOrNull() ?: 0f
                                    classification = getClassification(maxScore)
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


