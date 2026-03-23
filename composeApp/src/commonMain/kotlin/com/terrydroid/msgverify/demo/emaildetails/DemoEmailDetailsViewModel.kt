package com.terrydroid.msgverify.demo.emaildetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terrydroid.msgverify.data.ContentVerificationResponse
import com.terrydroid.msgverify.data.MsgVerifyRepository
import com.terrydroid.msgverify.demo.emailoverview.EmailMessage
import com.terrydroid.msgverify.demo.emailoverview.getEmail
import com.terrydroid.msgverify.demo.smsoverview.TrafficLight
import com.terrydroid.msgverify.demo.smsoverview.UrlScore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DemoEmailDetailsViewModel(
    private val msgVerifyRepository: MsgVerifyRepository,
    private val dispatcher: CoroutineDispatcher,
) : ViewModel() {
    private val _state: MutableStateFlow<EmailMessage?> = MutableStateFlow(null)
    val state = _state.asStateFlow()
    private var currentJob: Job? = null

    fun init(id: Int) {
        currentJob?.cancel()

        currentJob = viewModelScope.launch(dispatcher) {
            val email = getEmail(id)
            _state.value = email
            if (email != null) {
                withContext(viewModelScope.coroutineContext) {
                    msgVerifyRepository.verifyContent(email.body, email.fromName).collect { result ->
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

                                _state.value = email.copy(
                                    trafficLight = classification,
                                    reasons = reasons,
                                    urlScores = urlScores
                                )
                            },
                            onFailure = {}
                        )
                    }
                }
            }
        }
    }
}
