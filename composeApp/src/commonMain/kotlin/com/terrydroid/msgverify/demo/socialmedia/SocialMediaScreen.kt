package com.terrydroid.msgverify.demo.socialmedia

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.terrydroid.msgverify.demo.smsoverview.TrafficLight
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SocialMediaScreen(
    paddingValues: PaddingValues,
    viewModel: SocialMediaViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            ComposerBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            )
        }

        items(
            items = state.messages,
            key = { it.id }
        ) { msg ->
            MessageCard(
                message = msg,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
private fun ComposerBox(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AvatarCircle(initial = "U")
                Spacer(Modifier.width(10.dp))
                val input = remember { mutableStateOf("") }
                TextField(
                    placeholder = { Text("What's happening?") },
                    value = input.value,
                    onValueChange = { value: String ->
                        input.value = value
                    },
                )
            }

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(onClick = { /* later */ }) {
                    Text("Post")
                }
            }
        }
    }
}

@Composable
private fun MessageCard(
    message: Message,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.Top
            ) {
                AvatarCircle(initial = message.title.firstOrNull()?.toString() ?: "?")
                Spacer(Modifier.width(10.dp))

                Column(Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = message.title,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(Modifier.width(8.dp))

                        TrafficLightBadge(light = message.trafficLight)
                    }

                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = message.message,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    if (message.reasons.isNotEmpty()) {
                        Spacer(Modifier.height(10.dp))
                        ReasonsChips(reasons = message.reasons)
                    }

                    Spacer(Modifier.height(10.dp))

                    ActionsRow()
                }
            }
        }
    }
}

@Composable
private fun ActionsRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        TextButton(onClick = { /* reply */ }) { Text("Reply") }
        TextButton(onClick = { /* repost */ }) { Text("Repost") }
        TextButton(onClick = { /* like */ }) { Text("Like") }
        TextButton(onClick = { /* share */ }) { Text("Share") }
    }
}

@Composable
private fun AvatarCircle(initial: String) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initial,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TrafficLightBadge(light: TrafficLight?) {
    val (dotColor, label) = when (light) {
        TrafficLight.Green -> MaterialTheme.colorScheme.primary to "Looks safe"
        TrafficLight.Yellow -> MaterialTheme.colorScheme.tertiary to "Suspicious"
        TrafficLight.Red -> MaterialTheme.colorScheme.error to "Likely malicious"
        null -> MaterialTheme.colorScheme.outline to "Unknown"
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ReasonChip(reason: String) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Text(
            text = reason,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ReasonsChips(reasons: List<String>) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        reasons.forEach { reason ->
            ReasonChip(reason = reason)
            Spacer(Modifier.height(6.dp))
        }
    }
}

