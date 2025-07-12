package com.terrydroid.msgverify.home.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.terrydroid.msgverify.home.ClassificationColor
import com.terrydroid.msgverify.home.LinkResult
import com.terrydroid.msgverify.home.LinkVerificationState

internal fun LazyListScope.historicalLinkResults(
    linkVerificationState: LinkVerificationState, onShowBottomSheet: (link: LinkResult) -> Unit
) {
    items(linkVerificationState.verifiedLinkHistory) { item ->
        val icon = when (item.classificationColor) {
            ClassificationColor.Red -> Icons.Default.Error
            ClassificationColor.Yellow -> Icons.Default.Warning
            ClassificationColor.Green -> Icons.Default.Shield
        }
        Spacer(Modifier.size(8.dp))
        ElevatedCard(onClick = {
            onShowBottomSheet.invoke(item)
        }) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    modifier = Modifier.padding(4.dp), imageVector = icon, contentDescription = null
                )
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = item.url,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )

                Text(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    text = "Malicious Chance Percentage: ${item.linkMaliciousPercentage} %",
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}