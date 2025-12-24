package com.terrydroid.msgverify.demo

private val messages = Messages(
    messages = listOf(
        // Legit / low risk
        Message(
            id = 0,
            title = "Receipt: Coffee Corner",
            message = "Thanks for your purchase! Amount: 59 NOK. Date: 13 Dec 2025 09:12. Card: **42.",
        ),
        Message(
            id = 1,
            title = "Posten: Package delivered",
            message = "Your package has been delivered to pickup point 'Kiwi St. Hanshaugen'. Opening hours: 08–22.",
        ),
        Message(
            id = 2,
            title = "BankID: Login confirmation",
            message = "A login was completed using BankID on mobile at 10:41. If this wasn't you, contact support via the app.",
        ),
        Message(
            id = 3,
            title = "Norwegian: Flight reminder",
            message = "Reminder: DY604 departs 16:25 tomorrow. Check-in closes 45 minutes before departure.",
        ),

        // Slightly suspicious / could be legit but needs caution
        Message(
            id = 4,
            title = "Password reset request",
            message = "We received a request to reset your password. If this was you, use the code: 384921. If not, ignore.",
        ),
        Message(
            id = 5,
            title = "Unusual sign-in attempt",
            message = "We blocked a sign-in attempt from a new device (Chrome on Windows). Review recent activity in settings.",
        ),
        Message(
            id = 6,
            title = "Invoice available",
            message = "Your monthly invoice is ready. Log in to your customer portal to view it. (No attachments included.)",
        ),
        Message(
            id = 7,
            title = "Delivery address confirmation",
            message = "Your delivery address was updated recently. If you didn’t make this change, verify your account details.",
        ),

        // Phishing / high risk
        Message(
            id = 8,
            title = "Posten: Delivery failed — action required",
            message = "We couldn't deliver your parcel. Pay the missing fee (14,90 NOK) to reschedule: posten-track.help/NO-49302",
        ),
        Message(
            id = 9,
            title = "Bank alert: Account temporarily locked",
            message = "Your account has been locked due to suspicious activity. Verify immediately to restore access: secure-bankid-verify.com",
        ),
        Message(
            id = 10,
            title = "Apple Support: iCloud suspended",
            message = "Your iCloud has been suspended due to payment failure. Update billing within 12 hours to avoid data loss: apple-billing-portal.live",
        ),
        Message(
            id = 11,
            title = "Vipps: You've received 2,500 NOK",
            message = "You have a pending transfer. Confirm to receive funds: vipps-confirmation.net (expires in 30 minutes)",
        ),
        Message(
            id = 12,
            title = "Skatteetaten: Refund pending",
            message = "Tax refund pending. Submit your details to receive payout today: skatteetaten-refund.no/claim",
        ),
        Message(
            id = 13,
            title = "Microsoft Security: Virus detected!",
            message = "CRITICAL: Windows Defender found 5 threats. Click to remove now and renew protection: ms-defender-security-alert.info",
        )
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
