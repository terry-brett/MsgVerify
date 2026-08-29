package com.terrydroid.msgverify.demo.emailoverview

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.terrydroid.msgverify.demo.smsoverview.TrafficLight

@Composable
internal fun EmailListItem(
    message: EmailMessage,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 16.dp,
                    vertical = 12.dp,
                ),
            verticalAlignment = Alignment.Top,
        ) {

            SenderAvatar(
                name = message.fromName,
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = message.fromName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (message.unread) {
                            FontWeight.Bold
                        } else {
                            FontWeight.Normal
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    VerificationIndicator(
                        trafficLight = message.trafficLight,
                    )
                }

                Text(
                    text = message.subject,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (message.unread) {
                        FontWeight.SemiBold
                    } else {
                        FontWeight.Normal
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Text(
                    text = message.preview,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            IconButton(
                onClick = { /* Demo only */ },
                modifier = Modifier.size(40.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.StarBorder,
                    contentDescription = "Star",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(start = 68.dp),
            color = MaterialTheme.colorScheme.outlineVariant,
        )
    }
}

@Composable
private fun SenderAvatar(
    name: String,
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(
                MaterialTheme.colorScheme.secondaryContainer,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = name
                .trim()
                .take(1)
                .uppercase(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}

@Composable
private fun VerificationIndicator(
    trafficLight: TrafficLight?,
) {
    val icon = when (trafficLight) {
        TrafficLight.Green -> Icons.Outlined.Verified
        TrafficLight.Yellow -> Icons.Outlined.WarningAmber
        TrafficLight.Red -> Icons.Outlined.ErrorOutline
        null -> null
    }

    if (icon != null) {
        val tint = when (trafficLight) {
            TrafficLight.Green ->
                MaterialTheme.colorScheme.primary

            TrafficLight.Yellow ->
                MaterialTheme.colorScheme.tertiary

            TrafficLight.Red ->
                MaterialTheme.colorScheme.error

            null ->
                MaterialTheme.colorScheme.onSurfaceVariant
        }

        Icon(
            imageVector = icon,
            contentDescription = when (trafficLight) {
                TrafficLight.Green -> "Message appears safe"
                TrafficLight.Yellow -> "Suspicious message"
                TrafficLight.Red -> "Potential phishing"
                null -> null
            },
            tint = tint,
            modifier = Modifier.size(18.dp),
        )
    }
}