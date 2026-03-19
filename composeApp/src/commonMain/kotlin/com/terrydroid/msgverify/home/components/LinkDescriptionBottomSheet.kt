package com.terrydroid.msgverify.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.terrydroid.msgverify.home.ClassificationColor
import com.terrydroid.msgverify.home.components.LinkVerificationColors.getColorForLinkScore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LinkDescriptionBottomSheet(
    description: String,
    maliciousPercentScore: Float,
    onHideBottomSheet: () -> Unit,
    sheetState: SheetState,
    classificationColor: ClassificationColor,
    hasUrls: Boolean
) {
    ModalBottomSheet(
        onDismissRequest = {
            onHideBottomSheet.invoke()
        },
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 16.dp,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .width(50.dp)
                    .height(6.dp)
                    .clip(MaterialTheme.shapes.extraLarge)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            //TODO: Implement warning and success colors
            val textColor = getColorForLinkScore(classificationColor)

            if (hasUrls) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = "There is a $maliciousPercentScore % " +
                            "chance it's malicious",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium,
                    color = textColor
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = description,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
