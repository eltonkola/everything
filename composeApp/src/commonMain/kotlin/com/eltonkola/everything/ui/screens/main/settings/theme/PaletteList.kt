package com.eltonkola.everything.ui.screens.main.settings.theme

import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.materialkolor.PaletteStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaletteStyleDropdown(
    selectedStyle: PaletteStyle,
    onStyleSelected: (PaletteStyle) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        TextField(
            value = selectedStyle.getDisplayName(),
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            allPaletteStyles.forEach { style ->
                DropdownMenuItem(
                    text = { Text(style.getDisplayName()) },
                    onClick = {
                        onStyleSelected(style)
                        expanded = false
                    }
                )
            }
        }
    }
}

// List of all enum values (put this in a common file)
val allPaletteStyles = PaletteStyle.values().toList()

// Extension function for display names
fun PaletteStyle.getDisplayName(): String {
    return when (this) {
        PaletteStyle.TonalSpot -> "Tonal Spot"
        PaletteStyle.Neutral -> "Neutral"
        PaletteStyle.Vibrant -> "Vibrant"
        PaletteStyle.Expressive -> "Expressive"
        PaletteStyle.Rainbow -> "Rainbow"
        PaletteStyle.FruitSalad -> "Fruit Salad"
        PaletteStyle.Monochrome -> "Monochrome"
        PaletteStyle.Fidelity -> "Fidelity"
        PaletteStyle.Content -> "Content"
    }
}
