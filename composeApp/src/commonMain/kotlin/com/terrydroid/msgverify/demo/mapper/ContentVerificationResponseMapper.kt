package com.terrydroid.msgverify.demo.mapper

import com.terrydroid.msgverify.data.ContentVerificationResponse
import com.terrydroid.msgverify.demo.smsoverview.TrafficLight
import com.terrydroid.msgverify.demo.smsoverview.UrlScore

fun ContentVerificationResponse.toUiModels(): Triple<TrafficLight, List<String>, List<UrlScore>> {
    return when (this) {
        is ContentVerificationResponse.Safe -> Triple(TrafficLight.Green, emptyList(), emptyList())
        is ContentVerificationResponse.Unsafe -> {
            val scores = (extractedUrls ?: emptyList())
                .zip(urlScores ?: emptyList())
                .map { (url, score) -> UrlScore(url, score) }
            Triple(TrafficLight.Red, reasons.map { it.reason }, scores)
        }
    }
}
