package com.eltonkola.everything.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

data class SwipeAction(
    val icon: ImageVector,
    val label: String,
    val backgroundColor: Color,
    val contentColor: Color = Color.White,
    val onAction: () -> Unit
)

@Composable
fun SwipeActionItem(
    modifier: Modifier = Modifier,
    leftActions: List<SwipeAction> = emptyList(),
    rightActions: List<SwipeAction> = emptyList(),
    swipeThreshold: Float = 0.1f,
    actionWidth: Float = 110f,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val actionWidthPx = with(density) { actionWidth.dp.toPx() }
    val swipeableState = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()

    // Calculate max swipe distances
    val maxLeftSwipe = leftActions.size * actionWidthPx
    val maxRightSwipe = -rightActions.size * actionWidthPx

    Box(modifier = modifier.fillMaxWidth()) {
        // Background actions (left side)
        if (leftActions.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxHeight()
                    .width((leftActions.size * actionWidth).dp)
                    .align(Alignment.CenterStart),
                horizontalArrangement = Arrangement.Start
            ) {
                leftActions.forEach { action ->
                    ActionButton(
                        action = action,
                        width = actionWidth.dp,
                        onClick = {
                            coroutineScope.launch {
                                swipeableState.animateTo(0f, tween(200))
                            }
                            action.onAction()
                        }
                    )
                }
            }
        }

        // Background actions (right side)
        if (rightActions.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxHeight()
                    .width((rightActions.size * actionWidth).dp)
                    .align(Alignment.CenterEnd),
                horizontalArrangement = Arrangement.End
            ) {
                rightActions.forEach { action ->
                    ActionButton(
                        action = action,
                        width = actionWidth.dp,
                        onClick = {
                            coroutineScope.launch {
                                swipeableState.animateTo(0f, tween(200))
                            }
                            action.onAction()
                        }
                    )
                }
            }
        }

        // Main content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(swipeableState.value.roundToInt(), 0) }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            coroutineScope.launch {
                                val currentOffset = swipeableState.value
                                val threshold = size.width * swipeThreshold

                                when {
                                    // Snap to left actions
                                    currentOffset > threshold && leftActions.isNotEmpty() -> {
                                        swipeableState.animateTo(maxLeftSwipe, tween(300))
                                    }
                                    // Snap to right actions
                                    currentOffset < -threshold && rightActions.isNotEmpty() -> {
                                        swipeableState.animateTo(maxRightSwipe, tween(300))
                                    }
                                    // Snap back to center
                                    else -> {
                                        swipeableState.animateTo(0f, tween(300))
                                    }
                                }
                            }
                        }
                    ) { _, dragAmount ->
                        coroutineScope.launch {
                            val newValue = swipeableState.value + dragAmount
                            val constrainedValue = when {
                                newValue > maxLeftSwipe -> maxLeftSwipe
                                newValue < maxRightSwipe -> maxRightSwipe
                                else -> newValue
                            }
                            swipeableState.snapTo(constrainedValue)
                        }
                    }
                }
        ) {
            content()
        }
    }
}

@Composable
private fun ActionButton(
    action: SwipeAction,
    width: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxHeight()
            .width(width),
        colors = ButtonDefaults.buttonColors(
            containerColor = action.backgroundColor,
            contentColor = action.contentColor
        ),
        shape = RoundedCornerShape(0.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = action.icon,
                contentDescription = action.label,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = action.label,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}
