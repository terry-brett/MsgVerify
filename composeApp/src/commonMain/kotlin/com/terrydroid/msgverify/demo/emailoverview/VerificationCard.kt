package com.terrydroid.msgverify.demo.emaildetails

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.terrydroid.msgverify.demo.smsoverview.TrafficLight

@Composable
internal fun VerificationCard(
    trafficLight: TrafficLight,
    reasons: List<String>,
    modifier: Modifier = Modifier,
) {
    var expanded by rememberSaveable {
        mutableStateOf(false)
    }

    val isSafe = trafficLight == TrafficLight.Green
    val isHighRisk = trafficLight == TrafficLight.Red

    val title = when (trafficLight) {
        TrafficLight.Green ->
            "Message appears safe"

        TrafficLight.Yellow ->
            "Message requires caution"

        TrafficLight.Red ->
            "Potential phishing detected"
    }

    val subtitle = when (trafficLight) {
        TrafficLight.Green ->
            "No significant phishing indicators were detected."

        TrafficLight.Yellow ->
            "Some indicators associated with suspicious messages were detected."

        TrafficLight.Red ->
            "This message contains indicators commonly associated with phishing."
    }

    val icon = when (trafficLight) {
        TrafficLight.Green -> Icons.Outlined.Verified
        TrafficLight.Yellow -> Icons.Outlined.WarningAmber
        TrafficLight.Red -> Icons.Outlined.ErrorOutline
    }

    val containerColor = when (trafficLight) {
        TrafficLight.Green ->
            MaterialTheme.colorScheme.primaryContainer

        TrafficLight.Yellow ->
            MaterialTheme.colorScheme.tertiaryContainer

        TrafficLight.Red ->
            MaterialTheme.colorScheme.errorContainer
    }

    val contentColor = when (trafficLight) {
        TrafficLight.Green ->
            MaterialTheme.colorScheme.onPrimaryContainer

        TrafficLight.Yellow ->
            MaterialTheme.colorScheme.onTertiaryContainer

        TrafficLight.Red ->
            MaterialTheme.colorScheme.onErrorContainer
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = MaterialTheme.shapes.large,
        color = containerColor,
        tonalElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(24.dp),
                )

                Spacer(modifier = Modifier.size(12.dp))

                Column(
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        color = contentColor,
                    )

                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = contentColor.copy(alpha = 0.8f),
                    )
                }

                if (!isSafe && reasons.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            expanded = !expanded
                        },
                    ) {
                        Icon(
                            imageVector = if (expanded) {
                                Icons.Outlined.ExpandLess
                            } else {
                                Icons.Outlined.ExpandMore
                            },
                            contentDescription = if (expanded) {
                                "Hide verification details"
                            } else {
                                "Show verification details"
                            },
                            tint = contentColor,
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = expanded && !isSafe,
            ) {
                Column {
                    Spacer(modifier = Modifier.size(12.dp))

                    HorizontalDivider(
                        color = contentColor.copy(alpha = 0.2f),
                    )

                    Spacer(modifier = Modifier.size(12.dp))

                    Text(
                        text = "Why this message was flagged",
                        style = MaterialTheme.typography.labelLarge,
                        color = contentColor,
                    )

                    Spacer(modifier = Modifier.size(8.dp))

                    reasons.forEach { reason ->
                        Row(
                            modifier = Modifier.padding(
                                vertical = 3.dp,
                            ),
                            verticalAlignment = Alignment.Top,
                        ) {
                            Text(
                                text = "•",
                                color = contentColor,
                                modifier = Modifier.padding(end = 8.dp),
                            )

                            Text(
                                text = reason,
                                style = MaterialTheme.typography.bodySmall,
                                color = contentColor,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.size(12.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = null,
                            tint = contentColor,
                            modifier = Modifier.size(16.dp),
                        )

                        Text(
                            text = if (isHighRisk) {
                                "Avoid clicking links or providing personal information."
                            } else {
                                "Check the sender and links before taking action."
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = contentColor,
                        )
                    }
                }
            }
        }
    }
}