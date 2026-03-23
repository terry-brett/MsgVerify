package com.terrydroid.msgverify.home.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.terrydroid.msgverify.home.ClassificationColor

object LinkVerificationColors {

    @Composable
    fun getColorForLinkScore(classificationColor: ClassificationColor): Color {
        return when (classificationColor) {
            ClassificationColor.Red -> MaterialTheme.colorScheme.error
            ClassificationColor.Yellow -> Color(0xFFF59E0B)
            ClassificationColor.Green -> Color(0xFF22C55E)
        }
    }
}
