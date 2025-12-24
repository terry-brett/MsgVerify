package com.terrydroid.msgverify.demo

private val messages = Messages(
    messages = listOf(
        // Legit / low risk
        Message(
            id = 0,
            title = "Receipt: Coffee Corner",
            message = "Thanks for your purchase! Amount: 59 NOK. Date: 13 Dec 2025 09:12. Card: **42.",
        ),

        // Slightly suspicious / could be legit but needs caution
        Message(
            id = 1,
            title = "Password reset request",
            message = "We received a request to reset your password. If this was you, use the code: 384921. If not, ignore.",
        ),
        Message(
            id = 2,
            title = "Unusual sign-in attempt",
            message = "We blocked a sign-in attempt from a new device (Chrome on Windows). Review recent activity in settings.",
        ),

        // Phishing / high risk
        Message(
            id = 3,
            title = "Posten: Delivery failed — action required",
            message = "We couldn't deliver your parcel. Pay the missing fee (14,90 NOK) to reschedule: posten-track.help/NO-49302",
        ),
        Message(
            id = 4,
            title = "Bank alert: Account temporarily locked",
            message = "Your account has been locked due to suspicious activity. Verify immediately to restore access: secure-bankid-verify.com",
        ),
        Message(
            id = 5,
            title = "Apple Support: iCloud suspended",
            message = "Your iCloud has been suspended due to payment failure. Update billing within 12 hours to avoid data loss: apple-billing-portal.live",
        ),
        Message(
            id = 6,
            title = "Vipps: You've received 2,500 NOK",
            message = "You have a pending transfer. Confirm to receive funds: vipps-confirmation.net (expires in 30 minutes)",
        ),

    )
)
internal fun getMessagesMockdata(): Messages {
  return messages
}

internal fun getClassification(maliciousScorePercent: Float): TrafficLight {
    return if (maliciousScorePercent > 70) {
        TrafficLight.Green
    } else if (maliciousScorePercent >= 40 && maliciousScorePercent < 70) {
        TrafficLight.Yellow
    } else {
        TrafficLight.Red
    }
}

internal fun getMessage(id: Int): Message? {
    return messages.messages.find { it.id == id }
}
