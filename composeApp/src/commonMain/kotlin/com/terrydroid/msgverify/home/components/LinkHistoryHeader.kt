package com.terrydroid.msgverify.home.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.terrydroid.msgverify.home.LinkVerificationState

internal fun LazyListScope.linkHistoryHeader(
    linkVerificationState: LinkVerificationState
) {
    item {
        if (linkVerificationState.verifiedLinkHistory.isNotEmpty()) {
            Text(
                modifier = Modifier.padding(top = 24.dp),
                text = "Previously verified links",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}
