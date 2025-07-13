package com.terrydroid.msgverify.home.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.terrydroid.msgverify.home.ClassificationColor

object LinkVerificationColors {

    @Composable
    fun getColorForLinkScore(classificationColor: ClassificationColor): Color {
        //TODO: Implement warning and success colors
        return when (classificationColor) {
            ClassificationColor.Red -> MaterialTheme.colorScheme.error
            ClassificationColor.Yellow -> MaterialTheme.colorScheme.onBackground
            ClassificationColor.Green -> MaterialTheme.colorScheme.onBackground
        }
    }
}
