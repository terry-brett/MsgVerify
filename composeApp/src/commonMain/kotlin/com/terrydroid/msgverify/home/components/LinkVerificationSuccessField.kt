package com.terrydroid.msgverify.home.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.terrydroid.msgverify.home.LinkVerificationState

internal fun LazyListScope.linkVerificationSuccessField(
    linkVerificationState: LinkVerificationState.Success
) {
    item {
        val textColor = LinkVerificationColors.getColorForLinkScore(
            linkVerificationState.linkResult.classificationColor
        )

        Text(
            modifier = Modifier.fillMaxWidth(),
            text = "There is a ${linkVerificationState.linkResult.linkMaliciousPercentage} % " +
                    "chance it's malicious",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelMedium,
            color = textColor
        )
    }
}
