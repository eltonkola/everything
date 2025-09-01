package com.eltonkola.everything.ui.screens.main.settings.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Palette
import com.eltonkola.everything.ui.screens.main.settings.SettingsButton


@Composable
fun ThemePreviewCardButton() {
    var showThemePreview by remember { mutableStateOf(false) }

    SettingsButton(
        onClick = { showThemePreview = true },
        icon = Lucide.Palette,
        title = "Preview Theme",
        subtitle = "See how your theme looks on ui elements"
    )

    if (showThemePreview) {
        ThemePreviewSheet(onDismiss = { showThemePreview = false })
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemePreviewSheet(
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        dragHandle = {
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Theme Preview",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close preview",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = true)
            ) {
                Material3ThemeGallery()
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.End
            ) {

                Button(
                    onClick = {
                        onDismiss()
                    }
                ) {
                    Text("Close")
                }
            }
        }
    }
}

/**
 * A single Composable Column that drops most Material 3 components on the screen
 * so you can quickly eyeball your color roles, typography and shapes.
 *
 * Safe to drop into any Compose project using Material3.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Material3ThemeGallery(
    modifier: Modifier = Modifier
) {
    val scroll = rememberScrollState()

    // Simple state holders for interactive components
    var text by remember { mutableStateOf("") }
    var outlinedText by remember { mutableStateOf("") }
    var checked by remember { mutableStateOf(true) }
    var radio by remember { mutableStateOf("A") }
    var slider by remember { mutableStateOf(0.4f) }
    var range by remember { mutableStateOf(0.2f..0.8f) }
    var filterChipSelected by remember { mutableStateOf(true) }
    var inputChipSelected by remember { mutableStateOf(false) }

    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Typography sampler
        Text("Display Large", style = MaterialTheme.typography.displayLarge)
        Text("Headline Medium", style = MaterialTheme.typography.headlineMedium)
        Text("Title Large", style = MaterialTheme.typography.titleLarge)
        Text("Body Medium — the quick brown fox jumps over the lazy dog.", style = MaterialTheme.typography.bodyMedium)
        Text("Label Small", style = MaterialTheme.typography.labelSmall)

        Divider()

        // Buttons
        Text("Buttons", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = {}) { Text("Filled") }
            FilledTonalButton(onClick = {}) { Text("Tonal") }
            OutlinedButton(onClick = {}) { Text("Outlined") }
            ElevatedButton(onClick = {}) { Text("Elevated") }
            TextButton(onClick = {}) { Text("Text") }
        }

        // Icon buttons
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            IconButton(onClick = {}) { Icon(Icons.Filled.Settings, contentDescription = null) }
            FilledIconButton(onClick = {}) { Icon(Icons.Filled.Favorite, contentDescription = null) }
            OutlinedIconButton(onClick = {}) { Icon(Icons.Filled.Refresh, contentDescription = null) }
        }

        // FABs
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FloatingActionButton(onClick = {}) { Icon(Icons.Filled.Favorite, contentDescription = null) }
            SmallFloatingActionButton(onClick = {}) { Icon(Icons.Filled.Settings, contentDescription = null) }
            LargeFloatingActionButton(onClick = {}) { Icon(Icons.Filled.Refresh, contentDescription = null) }
            ExtendedFloatingActionButton(onClick = {}, icon = { Icon(Icons.Filled.Favorite, null) }, text = { Text("Extended FAB") })
        }

        Divider()

        // Text fields
        Text("TextFields", style = MaterialTheme.typography.titleMedium)
        TextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("TextField") },
            placeholder = { Text("Type something…") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = outlinedText,
            onValueChange = { outlinedText = it },
            label = { Text("OutlinedTextField") },
            trailingIcon = { Icon(Icons.Filled.Settings, contentDescription = null) },
            modifier = Modifier.fillMaxWidth()
        )

        // Selection controls
        Text("Selection Controls", style = MaterialTheme.typography.titleMedium)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Checkbox(checked = checked, onCheckedChange = { checked = it })
            Switch(checked = checked, onCheckedChange = { checked = it })
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = radio == "A", onClick = { radio = "A" })
                Text("A", modifier = Modifier.padding(end = 8.dp))
                RadioButton(selected = radio == "B", onClick = { radio = "B" })
                Text("B")
            }
        }

        // Sliders
        Text("Sliders", style = MaterialTheme.typography.titleMedium)
        Slider(value = slider, onValueChange = { slider = it })
        RangeSlider(value = range, onValueChange = { range = it })

        // Progress indicators
        Text("Progress", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator()
            LinearProgressIndicator(progress = { slider })
        }

        Divider()

        // Chips
        Text("Chips", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AssistChip(onClick = {}, label = { Text("Assist") }, leadingIcon = { Icon(Icons.Filled.Settings, null) })
            FilterChip(selected = filterChipSelected, onClick = { filterChipSelected = !filterChipSelected }, label = { Text("Filter") })
            SuggestionChip(onClick = {}, label = { Text("Suggestion") })
            InputChip(selected = inputChipSelected, onClick = { inputChipSelected = !inputChipSelected }, label = { Text("Input") })
        }

        // Badges
        Text("Badges", style = MaterialTheme.typography.titleMedium)
        BadgedBox(badge = { Badge { Text("9") } }) {
            Icon(Icons.Filled.Favorite, contentDescription = null)
        }

        Divider()

        // Cards
        Text("Cards", style = MaterialTheme.typography.titleMedium)
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ElevatedCard(Modifier.fillMaxWidth()) {
                ListItem(
                    headlineContent = { Text("ElevatedCard") },
                    supportingContent = { Text("With a ListItem inside to check paddings & text styles.") },
                    leadingContent = { Icon(Icons.Filled.Settings, contentDescription = null) }
                )
            }
            Card(Modifier.fillMaxWidth()) {
                ListItem(
                    headlineContent = { Text("Filled Card") },
                    supportingContent = { Text("Cards use surface container colors and elevation in M3.") },
                    overlineContent = { Text("Overline") }
                )
            }
            OutlinedCard(Modifier.fillMaxWidth()) {
                ListItem(
                    headlineContent = { Text("OutlinedCard") },
                    supportingContent = { Text("Great for low emphasis content.") }
                )
            }
        }

        // List items & dividers
        Text("List Items", style = MaterialTheme.typography.titleMedium)
        Column(Modifier.fillMaxWidth()) {
            ListItem(
                headlineContent = { Text("One-line list item") },
                leadingContent = { Icon(Icons.Filled.Settings, contentDescription = null) }
            )
            Divider()
            ListItem(
                headlineContent = { Text("Two-line list item", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                supportingContent = { Text("Secondary text to inspect contrast & sizing") },
                trailingContent = { AssistChip(onClick = {}, label = { Text("Action") }) }
            )
            Divider()
            ListItem(
                overlineContent = { Text("Overline") },
                headlineContent = { Text("Three-line list item") },
                supportingContent = { Text("Tertiary text to push line heights and spacing to the limit.") },
                leadingContent = {
                    Box(
                        Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.tertiaryContainer)
                    )
                }
            )
        }

        Spacer(Modifier.height(24.dp))
    }
}

