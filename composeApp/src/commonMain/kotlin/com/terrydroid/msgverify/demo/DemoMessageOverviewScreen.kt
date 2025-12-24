package com.terrydroid.msgverify.demo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun DemoMessagesScreen(
    paddingValues: PaddingValues,
    demoMessageOverviewViewModel: DemoMessageOverviewViewModel = koinViewModel(),
) {
    val state = demoMessageOverviewViewModel.state.collectAsStateWithLifecycle().value
    LazyColumn(contentPadding = paddingValues) {
        item {
            Spacer(modifier = Modifier.size(32.dp))
        }
        item {
            Text(
                modifier = Modifier.padding(8.dp),
                style = MaterialTheme.typography.headlineSmall,
                text = "This is mock data only verified with ContextGuard lib"
            )
        }

        item {
            Spacer(modifier = Modifier.size(32.dp))
        }

        items(state.messages) { message ->
            OutlinedCard(modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = message.title, style = MaterialTheme.typography.titleSmall)
                        Text(message.message, style = MaterialTheme.typography.bodyMedium)
                    }
                    Spacer(Modifier.width(12.dp))

                    when (message.trafficLight) {
                        TrafficLight.Red -> {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = "Message is probably malicious",
                                tint = Color.Red
                            )
                        }

                        TrafficLight.Yellow -> {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                tint = Color.Yellow,
                                contentDescription = "Message might be malicious"
                            )

                        }

                        TrafficLight.Green -> {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "Message should be fine",
                                tint = Color.Green
                            )
                        }

                        null -> {
                            /*NO-OP*/
                        }
                    }

                }
            }
        }
    }

}
