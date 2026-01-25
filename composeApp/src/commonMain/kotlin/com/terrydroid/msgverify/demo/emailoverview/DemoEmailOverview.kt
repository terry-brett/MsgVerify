package com.terrydroid.msgverify.demo.emailoverview

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ReportProblem
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.terrydroid.msgverify.demo.smsoverview.TrafficLight
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun DemoEmailOverview(
    padding: PaddingValues,
    detailsViewModel: DemoEmailOverviewViewModel = koinViewModel(),
) {
  val inbox = detailsViewModel.state.collectAsStateWithLifecycle().value.messages

  LazyColumn(
      verticalArrangement = Arrangement.spacedBy(10.dp),
      modifier = Modifier.fillMaxSize().padding(padding).padding(top = 24.dp),
  ) {
    item {
      Text(
          text = "Inbox",
          style = MaterialTheme.typography.headlineSmall,
          modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
      )
    }

    items(inbox, key = { it.id }) { msg ->
      EmailRow(
          message = msg,
          onClick = { /* TODO: navigate to details */ },
          modifier = Modifier.padding(horizontal = 16.dp),
      )
    }

    item { Spacer(Modifier.height(8.dp)) }
  }
}

@Composable
private fun EmailRow(message: EmailMessage, onClick: () -> Unit, modifier: Modifier = Modifier) {
  val badge = riskBadge(message.trafficLight ?: TrafficLight.Yellow)
  val icon =
      when (message.trafficLight) {
        TrafficLight.Green -> Icons.Outlined.Verified
        TrafficLight.Yellow -> Icons.Outlined.ReportProblem
        TrafficLight.Red -> Icons.Outlined.ReportProblem
        null -> Icons.Outlined.Verified
      }

  Surface(
      tonalElevation = 1.dp,
      shape = RoundedCornerShape(16.dp),
      modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
  ) {
    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
      // Simple “avatar” block
      Box(
          modifier =
              Modifier.size(44.dp)
                  .clip(RoundedCornerShape(12.dp))
                  .background(MaterialTheme.colorScheme.surfaceVariant),
          contentAlignment = Alignment.Center,
      ) {
        Text(
            text = message.fromName.take(1).uppercase(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
      }

      Spacer(Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
                text = message.fromName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (message.unread) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = message.fromEmail,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
          }

          Spacer(Modifier.width(8.dp))

          AssistChip(
              onClick = { /* no-op */ },
              label = { Text(badge.text) },
              leadingIcon = { Icon(imageVector = icon, contentDescription = null) },
          )
        }

        Text(
            text = message.subject,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (message.unread) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        Text(
            text = message.preview,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )

        if (message.trafficLight != TrafficLight.Green) {
          Spacer(Modifier.height(6.dp))
          Text(
              text = badge.hint,
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              maxLines = 2,
              overflow = TextOverflow.Ellipsis,
          )
        }
      }
    }
  }
}

private data class RiskBadge(val text: String, val hint: String)

private fun riskBadge(level: TrafficLight): RiskBadge =
    when (level) {
      TrafficLight.Green -> RiskBadge(text = "Looks safe", hint = "No obvious red flags.")
      TrafficLight.Yellow ->
          RiskBadge(
              text = "Suspicious",
              hint = "Check sender domain, urgency, and where links really go.",
          )
      TrafficLight.Red ->
          RiskBadge(
              text = "Likely phishing",
              hint = "High-risk cues: urgent action, credential request, sketchy domain.",
          )
    }
