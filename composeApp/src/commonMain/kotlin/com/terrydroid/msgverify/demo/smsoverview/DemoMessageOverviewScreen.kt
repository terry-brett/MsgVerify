package com.terrydroid.msgverify.demo.smsoverview

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Report
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun DemoMessagesScreen(
    paddingValues: PaddingValues,
    navigateToDetails: (id: Int) -> Unit,
    demoMessageOverviewViewModel: DemoMessageOverviewViewModel = koinViewModel(),
) {
    val state = demoMessageOverviewViewModel.state.collectAsStateWithLifecycle().value
    LazyColumn(
        contentPadding = paddingValues,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Spacer(modifier = Modifier.size(32.dp))
        }
        item {
            ElevatedCard(
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Text(
                    modifier = Modifier.padding(8.dp),
                    style = MaterialTheme.typography.labelLarge,
                    text = "This is mock data only verified with ContextGuard lib"
                )
            }
        }

        item {
            Spacer(modifier = Modifier.size(24.dp))
        }

        items(
            items = state.messages,
            key = { it.id }
        ) { message ->
            OutlinedCard(
                modifier = Modifier.fillMaxWidth().padding(4.dp),
                shape = RoundedCornerShape(18.dp),
                onClick = { navigateToDetails(message.id) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left content
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = message.title,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = message.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    Column(horizontalAlignment = Alignment.End) {
                        when (message.trafficLight) {
                            TrafficLight.Red -> {
                                Icon(
                                    imageVector = Icons.Outlined.Report,
                                    contentDescription = "High risk",
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "High",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }

                            TrafficLight.Yellow -> {
                                Icon(
                                    imageVector = Icons.Outlined.WarningAmber,
                                    contentDescription = "Suspicious",
                                    tint = MaterialTheme.colorScheme.tertiary
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "Check",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }

                            TrafficLight.Green -> {
                                Icon(
                                    imageVector = Icons.Outlined.Shield,
                                    contentDescription = "Safe",
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "Safe",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }

                            null -> {
                                Icon(
                                    imageVector = Icons.Outlined.HelpOutline,
                                    contentDescription = "Unverified",
                                    tint = MaterialTheme.colorScheme.outline
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "N/A",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
