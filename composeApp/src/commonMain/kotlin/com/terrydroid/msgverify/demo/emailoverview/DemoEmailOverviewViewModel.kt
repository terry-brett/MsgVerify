package com.terrydroid.msgverify.demo.emailoverview

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terrydroid.msgverify.data.MsgVerifyRepository
import com.terrydroid.msgverify.demo.smsoverview.TrafficLight
import com.terrydroid.msgverify.demo.smsoverview.getClassification
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class DemoEmailOverviewViewModel(
    private val msgVerifyRepository: MsgVerifyRepository,
    dispatcher: CoroutineDispatcher
) : ViewModel() {
    private val _state: MutableStateFlow<Messages> = MutableStateFlow(messages)

    val state: StateFlow<Messages>
        get() = _state.asStateFlow()

    init {
        viewModelScope.launch(dispatcher) {
            state.value.messages.forEach { message ->
                msgVerifyRepository.verifyContent(message.preview).collect { result ->
                    result.fold(
                        onFailure = {},
                        onSuccess = { value ->
                            val maliciousScorePercent = value.maliciousScore

                            val classification = getClassification(maliciousScorePercent)
                            _state.update {
                                Messages(
                                    it.messages.replace(
                                        EmailMessage(
                                            id = message.id,
                                            fromName = message.fromName,
                                            fromEmail = message.fromEmail,
                                            subject = message.subject,
                                            preview = message.preview,
                                            unread = message.unread,
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


@Immutable
data class Messages(val messages: List<EmailMessage>)

@Immutable
data class EmailMessage(
    val id: Int,
    val fromName: String,
    val fromEmail: String,
    val subject: String,
    val preview: String,
    val unread: Boolean,
    val trafficLight: TrafficLight,
)


private val messages = Messages(
    messages = listOf(
        // CLEAR / LEGIT
        EmailMessage(
            id = 1,
            fromName = "Nordic Rail",
            fromEmail = "receipts@nordicrail.no",
            subject = "Your ticket receipt (Order #NR-10482)",
            preview = "Thanks for traveling with us. Your receipt and itinerary are attached. Total: NOK 429.00.",
            unread = true,
            trafficLight = TrafficLight.Green
        ),

        // SUSPICIOUS / MIGHT BE PHISHING
        EmailMessage(
            id = 4,
            fromName = "Payment Support",
            fromEmail = "support@paypa1-secure.com",
            subject = "Issue with your recent transaction",
            preview = "We couldn’t verify a transaction. Please review to avoid account limitation.",
            unread = true,
            trafficLight = TrafficLight.Yellow
        ),
        EmailMessage(
            id = 5,
            fromName = "IT Helpdesk",
            fromEmail = "it-helpdesk@yourcompany-support.com",
            subject = "Action required: mailbox storage over quota",
            preview = "Your mailbox has exceeded the limit. Click to increase storage and prevent incoming mail from bouncing.",
            unread = true,
            trafficLight = TrafficLight.Yellow
        ),
        EmailMessage(
            id = 6,
            fromName = "Delivery Update",
            fromEmail = "no-reply@dhl-tracking-info.com",
            subject = "Package pending: address confirmation needed",
            preview = "Your parcel cannot be delivered. Confirm address details to reschedule delivery.",
            unread = false,
            trafficLight = TrafficLight.Yellow
        ),

        // CLEAR PHISHING
        EmailMessage(
            id = 7,
            fromName = "Microsoft Account Team",
            fromEmail = "security@microsoft-reset-now.io",
            subject = "Final warning: password expires today",
            preview = "Your password will expire in 2 hours. Verify your identity now to avoid account lockout.",
            unread = true,
            trafficLight = TrafficLight.Red
        ),
        EmailMessage(
            id = 8,
            fromName = "CEO (Urgent)",
            fromEmail = "ceo.office@yourcompany-gifts.com",
            subject = "Need a quick favor—buy gift cards now",
            preview = "In a meeting. Please buy 6 gift cards and send codes ASAP. Keep this confidential.",
            unread = true,
            trafficLight = TrafficLight.Red
        ),
        EmailMessage(
            id = 9,
            fromName = "BankID",
            fromEmail = "alert@bankid-verification.net",
            subject = "Verify now to avoid suspension",
            preview = "We detected unusual activity. Confirm your BankID immediately to restore full access.",
            unread = true,
            trafficLight = TrafficLight.Red
        ),
    )
)
