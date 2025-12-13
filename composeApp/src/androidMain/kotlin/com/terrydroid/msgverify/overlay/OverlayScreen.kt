package com.terrydroid.msgverify.overlay

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Suppress("ComplexMethod")
@Composable
fun OverlayScreen(
    onCancel: () -> Unit,
    onDragStarted: () -> Pair<Offset, Int>,
    onDragEnd: (Offset, shouldRemoveView: Boolean) -> Unit,
) {
    var offsetX by rememberSaveable { mutableFloatStateOf(0f) }
    var offsetY by rememberSaveable { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    var cancelPosition by remember { mutableStateOf(Rect.Zero) }
    var boxPosition by remember { mutableStateOf(Rect.Zero) }
    val isOverlapping by remember {
        derivedStateOf {
            boxPosition.bottom >= cancelPosition.top
        }
    }

    Box {
        Box(modifier = Modifier
            .offset {
                IntOffset(
                    offsetX.roundToInt(),
                    offsetY.roundToInt()
                )
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        isDragging = true
                        val (offsetInScreen, statusBarHeight) = onDragStarted()
                        offsetX = offsetInScreen.x
                        offsetY = offsetInScreen.y - statusBarHeight
                    },
                    onDragEnd = {
                        isDragging = false
                        onDragEnd(
                            Offset(offsetX, offsetY),
                            isOverlapping
                        )
                        offsetX = 0f
                        offsetY = 0f
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        offsetX = (offsetX + dragAmount.x)
                        offsetY = (offsetY + dragAmount.y)
                    },
                    onDragCancel = { onCancel() }
                )
            }
            .onGloballyPositioned {
                boxPosition = it.boundsInParent()
            }
        ) {
            Overlay(
                isAboutToRemove = isOverlapping && isDragging,
            )
        }

        if (isDragging) {
            // A gradient background to make sure we have enough contrast for the content
            val colorStops = arrayOf(
                0f to Color.Transparent,
                0.5f to Color.Black.copy(alpha = 0.3f),
                1f to Color.Black.copy(alpha = 0.6f),
            )

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .height(100.dp)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(colorStops = colorStops)
                    )
                    .onGloballyPositioned {
                        cancelPosition = it.boundsInParent()
                    },
            ) {
                // This value is used to make sure that the haptic feedback is given only one time
                var resetHapticFeedback by remember {
                    mutableStateOf(false)
                }

                var isHapticFeedbackGiven by remember {
                    mutableStateOf(false)
                }

                LaunchedEffect(Unit) {
                    if (resetHapticFeedback) {
                        isHapticFeedbackGiven = false
                        resetHapticFeedback = false
                    }

                    if (!isOverlapping) {
                        resetHapticFeedback = true
                    }
                }

                if (isOverlapping && !isHapticFeedbackGiven) {
                    isHapticFeedbackGiven = true
                    LocalHapticFeedback.current.performHapticFeedback(
                        HapticFeedbackType.LongPress
                    )
                }

                Icon(
                    modifier = Modifier
                        .padding(16.dp)
                        .size(48.dp)

                        .align(Alignment.Center),
                    imageVector = Icons.Rounded.Close,
                    contentDescription = null,
                )
            }
        }
    }
}

@Composable
private fun Overlay(
    modifier: Modifier = Modifier,
    isAboutToRemove: Boolean,
) {
    val analyticsBoxHeight = remember { mutableStateOf(150.dp) }
    val expanded = rememberSaveable { mutableStateOf(false) }

    val backgroundColor = if (isAboutToRemove) {
        // Just make the background a bit reddish when removing
        val red = if (isSystemInDarkTheme()) 1f else 0.3f
        Color.Red
    } else MaterialTheme.colorScheme.background

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .shadow(
                elevation = 1.dp,
                shape = RoundedCornerShape(8.dp)
            ),
    ) {
        val analyticsContentModifier = Modifier
            .animateContentSize(
                spring(stiffness = Spring.StiffnessLow)
            )
            .clickable { expanded.value = !expanded.value }

        if (expanded.value) {
            Column(
                modifier = analyticsContentModifier
                    .width(300.dp)
                    .height(analyticsBoxHeight.value)
                    .background(backgroundColor)
                    .verticalScroll(rememberScrollState())
            ) {

            }
        } else {
            Icon(
                modifier = analyticsContentModifier
                    .background(backgroundColor)
                    .padding(16.dp),
                imageVector = Icons.AutoMirrored.Rounded.List,
                contentDescription = null
            )
        }
    }
}

