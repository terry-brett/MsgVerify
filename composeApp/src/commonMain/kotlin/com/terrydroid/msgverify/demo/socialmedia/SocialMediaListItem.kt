package com.terrydroid.msgverify.demo.socialmedia

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
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.terrydroid.msgverify.demo.smsoverview.TrafficLight
import io.github.feliperce.avatarkt.Avatar
import io.github.feliperce.avatarkt.AvatarVariant

@Composable
internal fun SocialMediaListItem(
    message: Message,
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
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Avatar(
                name = message.title,
                variant = AvatarVariant.BEAM,
                size = 52.dp,
                shape = CircleShape,
            )

            Spacer(
                modifier = Modifier.width(12.dp),
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = message.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )

                    Spacer(
                        modifier = Modifier.width(8.dp),
                    )

                    VerificationIndicator(
                        trafficLight = message.trafficLight,
                    )
                }

                Text(
                    text = message.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(
                start = 72.dp,
            ),
            color = MaterialTheme.colorScheme.outlineVariant,
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
                MaterialTheme.colorScheme.outline
        }

        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(18.dp),
        )
    }
}