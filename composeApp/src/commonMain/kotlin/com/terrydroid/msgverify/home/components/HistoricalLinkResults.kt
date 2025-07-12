package com.terrydroid.msgverify.home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.terrydroid.msgverify.home.ClassificationColor
import com.terrydroid.msgverify.home.LinkResult
import com.terrydroid.msgverify.home.LinkVerificationState

internal fun LazyListScope.historicalLinkResults(
    linkVerificationState: LinkVerificationState,
    onShowBottomSheet: (link: LinkResult) -> Unit
) {
    items(linkVerificationState.verifiedLinkHistory) { item ->
        Row(
            modifier = Modifier.fillMaxWidth().clickable(onClick = {
                onShowBottomSheet.invoke(item)
            }),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = item.url,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.size(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                val icon = when (item.classificationColor) {
                    ClassificationColor.Red -> Icons.Default.Error
                    ClassificationColor.Yellow -> Icons.Default.Warning
                    ClassificationColor.Green -> Icons.Default.Shield
                }
                Icon(
                    modifier = Modifier.padding(4.dp),
                    imageVector = icon,
                    contentDescription = null
                )
                Text(
                    text = "${item.linkMaliciousPercentage} %",
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}