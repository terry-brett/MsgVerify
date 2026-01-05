package com.terrydroid.msgverify.demo.overview

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun DemoOverviewScreen(
    onSmsDemoClicked: () -> Unit,
    onEmailDemoClicked: () -> Unit
) {
    LazyColumn {
        item {
            Spacer(Modifier.size(34.dp))
        }
        item {
            Text(
                modifier = Modifier.padding(4.dp),
                text = "Demos",
                style = MaterialTheme.typography.headlineSmall
            )
        }
        item {
            Spacer(Modifier.size(16.dp))
        }
        item {
            OutlinedCard(
                modifier = Modifier.fillMaxWidth().padding(4.dp),
                shape = RoundedCornerShape(18.dp),
                onClick = onSmsDemoClicked
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 8.dp).padding(horizontal = 4.dp)
                ) {
                    Text(
                        modifier = Modifier.padding(bottom = 4.dp),
                        text = "SMS Demo",
                        style = MaterialTheme.typography.labelMedium,
                    )

                    Text(
                        text = "Small demo on how to use context guard in context of an SMS application",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        item {
            OutlinedCard(
                modifier = Modifier.fillMaxWidth().padding(4.dp),
                shape = RoundedCornerShape(18.dp),
                onClick = onEmailDemoClicked
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 8.dp).padding(horizontal = 4.dp)
                ) {
                    Text(
                        modifier = Modifier.padding(bottom = 4.dp),
                        text = "Email Demo",
                        style = MaterialTheme.typography.labelMedium,
                    )

                    Text(
                        text = "Small demo on how to use context guard in context of an email application",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
