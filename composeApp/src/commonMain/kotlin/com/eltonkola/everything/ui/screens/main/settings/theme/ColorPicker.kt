package com.eltonkola.everything.ui.screens.main.settings.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.*

private fun rgbToHsv(r: Float, g: Float, b: Float): FloatArray {
    val hsv = FloatArray(3)
    val max = maxOf(r, g, b)
    val min = minOf(r, g, b)
    val delta = max - min

    // Calculate hue
    hsv[0] = when (max) {
        min -> 0f
        r -> ((g - b) / delta) % 6f
        g -> (b - r) / delta + 2f
        else -> (r - g) / delta + 4f
    } * 60f
    if (hsv[0] < 0) hsv[0] += 360f

    // Calculate saturation
    hsv[1] = if (max == 0f) 0f else delta / max

    // Value is just the max component
    hsv[2] = max

    return hsv
}

@Composable
fun ColorPicker(
    selectedColor: Color,
    onColorChanged: (Color) -> Unit,
    modifier: Modifier = Modifier
) {
    var hue by remember { mutableFloatStateOf(0f) }
    var saturation by remember { mutableFloatStateOf(1f) }
    var brightness by remember { mutableFloatStateOf(1f) }
    var alpha by remember { mutableFloatStateOf(1f) }

    // Initialize HSB values from the selected color
    LaunchedEffect(selectedColor) {
        val hsv = rgbToHsv(selectedColor.red, selectedColor.green, selectedColor.blue)
        hue = hsv[0]
        saturation = hsv[1]
        brightness = hsv[2]
        alpha = selectedColor.alpha
    }

    // Update color when HSB values change
    LaunchedEffect(hue, saturation, brightness, alpha) {
        val color = Color.hsv(hue, saturation, brightness, alpha)
        onColorChanged(color)
    }

    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Color preview
        ColorPreview(
            color = selectedColor,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
        )

        // HSV Color Wheel
        ColorWheel(
            hue = hue,
            saturation = saturation,
            onHueSaturationChanged = { newHue, newSaturation ->
                hue = newHue
                saturation = newSaturation
            },
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
        )

        // Brightness slider
        ColorSlider(
            value = brightness,
            onValueChange = { brightness = it },
            colors = listOf(
                Color.hsv(hue, saturation, 0f),
                Color.hsv(hue, saturation, 1f)
            ),
            label = "Brightness"
        )

        // Alpha slider
        ColorSlider(
            value = alpha,
            onValueChange = { alpha = it },
            colors = listOf(
                Color.hsv(hue, saturation, brightness, 0f),
                Color.hsv(hue, saturation, brightness, 1f)
            ),
            label = "Alpha",
            showCheckerboard = true
        )

        // Color values display
        ColorValues(selectedColor)

        // Predefined colors
        PredefinedColors(onColorSelected = onColorChanged)
    }
}

@Composable
private fun ColorPreview(
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Color.White, Color.LightGray),
                    start = Offset(0f, 0f),
                    end = Offset(50f, 50f)
                )
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color)
                .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
        )
    }
}

@Composable
private fun ColorWheel(
    hue: Float,
    saturation: Float,
    onHueSaturationChanged: (Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current

    Canvas(
        modifier = modifier
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val radius = minOf(size.width, size.height) / 2f
                    val offset = change.position - center
                    val distance = sqrt(offset.x * offset.x + offset.y * offset.y)

                    if (distance <= radius) {
                        val newSaturation = (distance / radius).coerceIn(0f, 1f)
                        val newHue = ((atan2(offset.y, offset.x) * 180 / PI + 360) % 360).toFloat()
                        onHueSaturationChanged(newHue, newSaturation)
                    }
                }
            }
    ) {
        drawColorWheel(hue, saturation)
    }
}

private fun DrawScope.drawColorWheel(selectedHue: Float, selectedSaturation: Float) {
    val center = Offset(size.width / 2f, size.height / 2f)
    val radius = minOf(size.width, size.height) / 2f

    // Draw the color wheel
    for (angle in 0 until 360) {
        val startAngle = angle.toFloat()
        val color = Color.hsv(startAngle, 1f, 1f)

        drawArc(
            color = color,
            startAngle = startAngle - 90f,
            sweepAngle = 1f,
            useCenter = true,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2)
        )
    }

    // Draw saturation gradient (white to transparent overlay)
    val saturationGradient = Brush.radialGradient(
        colors = listOf(Color.White, Color.Transparent),
        center = center,
        radius = radius
    )

    drawCircle(
        brush = saturationGradient,
        radius = radius,
        center = center
    )

    // Draw selection indicator
    val selectedAngle = (selectedHue + 90) * PI / 180
    val selectedRadius = selectedSaturation * radius
    val selectedPosition = Offset(
        center.x + (selectedRadius * cos(selectedAngle)).toFloat(),
        center.y + (selectedRadius * sin(selectedAngle)).toFloat()
    )

    drawCircle(
        color = Color.White,
        radius = 8.dp.toPx(),
        center = selectedPosition
    )
    drawCircle(
        color = Color.Black,
        radius = 6.dp.toPx(),
        center = selectedPosition
    )
}

@Composable
private fun ColorSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    colors: List<Color>,
    label: String,
    showCheckerboard: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "$label: ${(value * 100).toInt()}%",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
        ) {
            // Checkerboard background for alpha
            if (showCheckerboard) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    drawCheckerboard()
                }
            }

            // Gradient background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        brush = Brush.horizontalGradient(colors)
                    )
            )

            // Slider
            Slider(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxSize(),
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = Color.Transparent,
                    inactiveTrackColor = Color.Transparent
                )
            )
        }
    }
}

private fun DrawScope.drawCheckerboard() {
    val squareSize = 8.dp.toPx()
    val numSquaresX = (size.width / squareSize).toInt() + 1
    val numSquaresY = (size.height / squareSize).toInt() + 1

    for (i in 0..numSquaresX) {
        for (j in 0..numSquaresY) {
            val isLight = (i + j) % 2 == 0
            val color = if (isLight) Color.White else Color.LightGray

            drawRect(
                color = color,
                topLeft = Offset(i * squareSize, j * squareSize),
                size = Size(squareSize, squareSize)
            )
        }
    }
}

@Composable
private fun ColorValues(color: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Color Values",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )

            val hex = "#${color.toArgb().toUInt().toString(16).padStart(8, '0').uppercase()}"
            val rgb = "RGB(${(color.red * 255).toInt()}, ${(color.green * 255).toInt()}, ${(color.blue * 255).toInt()})"
            val alpha = "Alpha: ${(color.alpha * 100).toInt()}%"

            Text(text = "HEX: $hex", style = MaterialTheme.typography.bodySmall)
            Text(text = rgb, style = MaterialTheme.typography.bodySmall)
            Text(text = alpha, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun PredefinedColors(
    onColorSelected: (Color) -> Unit,
    modifier: Modifier = Modifier
) {
    val predefinedColors = listOf(
        Color.Red, Color.Green, Color.Blue, Color.Yellow,
        Color.Magenta, Color.Cyan, Color.Black, Color.White,
        Color.Gray, Color(0xFF800080), Color(0xFFFFA500), Color(0xFF008080)
    )

    Column(modifier = modifier) {
        Text(
            text = "Predefined Colors",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(predefinedColors) { color ->
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(color)
                        .border(1.dp, Color.Gray, CircleShape)
                        .clickable { onColorSelected(color) }
                )
            }
        }
    }
}

@Composable
fun ColorPickerButton(
    selectedColor: Color,
    onColorChanged: (Color) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: Dp = 48.dp,
    shape: Shape = RoundedCornerShape(8.dp),
    showBorder: Boolean = true
) {
    var showDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(
                // Checkerboard background for transparency
                brush = Brush.linearGradient(
                    colors = listOf(Color.White, Color.LightGray),
                    start = Offset(0f, 0f),
                    end = Offset(20f, 20f)
                )
            )
            .clickable(enabled = enabled) { showDialog = true }
    ) {
        // Color overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(selectedColor)
                .then(
                    if (showBorder) {
                        Modifier.border(1.dp, MaterialTheme.colorScheme.outline, shape)
                    } else {
                        Modifier
                    }
                )
        )

        // Disabled overlay
        if (!enabled) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
            )
        }
    }

    if (showDialog) {
        ColorPickerDialog(
            initialColor = selectedColor,
            onColorSelected = { color ->
                onColorChanged(color)
                showDialog = false
            },
            onDismiss = { showDialog = false }
        )
    }
}

@Composable
private fun ColorPickerDialog(
    initialColor: Color,
    onColorSelected: (Color) -> Unit,
    onDismiss: () -> Unit
) {
    var currentColor by remember { mutableStateOf(initialColor) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Choose Color",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            ColorPicker(
                selectedColor = currentColor,
                onColorChanged = { currentColor = it },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onColorSelected(currentColor) }
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancel")
            }
        }
    )
}

// Usage examples
@Composable
fun ColorPickerDemo() {
    var selectedColor by remember { mutableStateOf(Color.Red) }

    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Color Picker Button Demo",
            style = MaterialTheme.typography.headlineSmall
        )

        // Basic usage
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ColorPickerButton(
                selectedColor = selectedColor,
                onColorChanged = { selectedColor = it }
            )
            Text("Selected Color")
        }

        // Different sizes and shapes
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ColorPickerButton(
                selectedColor = selectedColor,
                onColorChanged = { selectedColor = it },
                size = 32.dp,
                shape = CircleShape
            )
            ColorPickerButton(
                selectedColor = selectedColor,
                onColorChanged = { selectedColor = it },
                size = 48.dp,
                shape = RoundedCornerShape(12.dp)
            )
            ColorPickerButton(
                selectedColor = selectedColor,
                onColorChanged = { selectedColor = it },
                size = 64.dp,
                shape = RoundedCornerShape(4.dp),
                showBorder = false
            )
        }

        // Disabled state
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ColorPickerButton(
                selectedColor = selectedColor,
                onColorChanged = { selectedColor = it },
                enabled = false
            )
            Text("Disabled")
        }
    }
}