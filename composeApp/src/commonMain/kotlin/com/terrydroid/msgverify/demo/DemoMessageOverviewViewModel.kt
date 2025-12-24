package com.terrydroid.msgverify.demo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terrydroid.msgverify.data.MsgVerifyRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DemoMessageOverviewViewModel(
    private val msgVerifyRepository: MsgVerifyRepository,
    dispatcher: CoroutineDispatcher
) : ViewModel() {
    private val _state: MutableStateFlow<Messages> = MutableStateFlow(getMockdata())

    val state: StateFlow<Messages>
        get() = _state.asStateFlow()

    init {
        viewModelScope.launch(dispatcher) {
            state.value.messages.forEach { message ->
                msgVerifyRepository.verifyContent(message.message).collect { result ->
                    result.fold(
                        onFailure = {},
                        onSuccess = { value ->
                            val maliciousScorePercent = value.maliciousScore

                            val classification = if (maliciousScorePercent > 70) {
                                TrafficLight.Green
                            } else if (maliciousScorePercent >= 40 && maliciousScorePercent < 70) {
                                TrafficLight.Yellow
                            } else {
                                TrafficLight.Red
                            }
                            print("Malicious Score: $maliciousScorePercent")
                            _state.update {
                                Messages(
                                    it.messages.replace(
                                        Message(
                                            title = message.title,
                                            message = message.message,
                                            trafficLight = classification
                                        )
                                    ) { value ->
                                        value == message
                                    }
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}

private fun <T> List<T>.replace(newValue: T, block: (T) -> Boolean): List<T> {
    return map {
        if (block(it)) newValue else it
    }
}

private fun getMockdata(): Messages {
    return Messages(
        messages = listOf(
            // Legit / low risk
            Message(
                title = "Receipt: Coffee Corner",
                message = "Thanks for your purchase! Amount: 59 NOK. Date: 13 Dec 2025 09:12. Card: **42.",
            ),
            Message(
                title = "Posten: Package delivered",
                message = "Your package has been delivered to pickup point 'Kiwi St. Hanshaugen'. Opening hours: 08–22.",
            ),
            Message(
                title = "BankID: Login confirmation",
                message = "A login was completed using BankID on mobile at 10:41. If this wasn't you, contact support via the app.",
            ),
            Message(
                title = "Norwegian: Flight reminder",
                message = "Reminder: DY604 departs 16:25 tomorrow. Check-in closes 45 minutes before departure.",
            ),

            // Slightly suspicious / could be legit but needs caution
            Message(
                title = "Password reset request",
                message = "We received a request to reset your password. If this was you, use the code: 384921. If not, ignore.",
            ),
            Message(
                title = "Unusual sign-in attempt",
                message = "We blocked a sign-in attempt from a new device (Chrome on Windows). Review recent activity in settings.",
            ),
            Message(
                title = "Invoice available",
                message = "Your monthly invoice is ready. Log in to your customer portal to view it. (No attachments included.)",
            ),
            Message(
                title = "Delivery address confirmation",
                message = "Your delivery address was updated recently. If you didn’t make this change, verify your account details.",
            ),

            // Phishing / high risk
            Message(
                title = "Posten: Delivery failed — action required",
                message = "We couldn't deliver your parcel. Pay the missing fee (14,90 NOK) to reschedule: posten-track.help/NO-49302",
            ),
            Message(
                title = "Bank alert: Account temporarily locked",
                message = "Your account has been locked due to suspicious activity. Verify immediately to restore access: secure-bankid-verify.com",
            ),
            Message(
                title = "Apple Support: iCloud suspended",
                message = "Your iCloud has been suspended due to payment failure. Update billing within 12 hours to avoid data loss: apple-billing-portal.live",
            ),
            Message(
                title = "Vipps: You've received 2,500 NOK",
                message = "You have a pending transfer. Confirm to receive funds: vipps-confirmation.net (expires in 30 minutes)",
            ),
            Message(
                title = "Skatteetaten: Refund pending",
                message = "Tax refund pending. Submit your details to receive payout today: skatteetaten-refund.no/claim",
            ),
            Message(
                title = "Microsoft Security: Virus detected!",
                message = "CRITICAL: Windows Defender found 5 threats. Click to remove now and renew protection: ms-defender-security-alert.info",
            )
        )
    )
}


data class Messages(
    val messages: List<Message>,
)

data class Message(
    val title: String,
    val message: String,
    val trafficLight: TrafficLight? = null,
)

enum class TrafficLight {
    Red, Yellow, Green
}