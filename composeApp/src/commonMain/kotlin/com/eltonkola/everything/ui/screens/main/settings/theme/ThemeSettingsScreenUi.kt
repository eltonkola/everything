package com.eltonkola.everything.ui.screens.main.settings.theme


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import co.touchlab.kermit.Logger
import com.composables.icons.lucide.ChevronLeft
import com.composables.icons.lucide.Lucide
import com.eltonkola.everything.data.local.AppSettings
import com.materialkolor.PaletteStyle
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSettingsScreenUi(
    navController: NavController,
    viewModel : ThemeSettingsViewModel = koinViewModel(),
) {

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Theme Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Lucide.ChevronLeft, contentDescription = "Back")
                    }
                }
            )
        }
    ) {

        Column(
            modifier = Modifier.padding(it)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            val themeState by viewModel.settingsState.collectAsState()

            Text("Appearance", style = MaterialTheme.typography.titleMedium)

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                SettingSwitch(
                    label = "Use System Theme",
                    checked = themeState.system,
                    onToggle = { viewModel.setSystemTheme(it) }
                )

                if (!themeState.system) {
                    SettingSwitch(
                        label = "Enable Dark Theme",
                        checked = themeState.dark,
                        onToggle = { viewModel.setDarkTheme(it) }
                    )
                }

                SettingSwitch(
                    label = "Use Expressive Motion",
                    checked = themeState.expressiveMotion,
                    onToggle = { viewModel.setExpressiveMotion(it) }
                )

                SettingSwitch(
                    label = "Animate",
                    checked = themeState.animate,
                    onToggle = { viewModel.setAnimate(it) }
                )


                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Primary theme color", style = MaterialTheme.typography.bodyLarge)
                    ColorPickerButton(
                        modifier = Modifier.padding(end = 8.dp),
                        size = 32.dp,
                        selectedColor = themeState.primaryColor,
                        onColorChanged = {
                            viewModel.setPrimaryColor(it)
                        }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Palette", style = MaterialTheme.typography.bodyLarge)
                    PaletteStyleDropdown(
                        modifier = Modifier.padding(end = 8.dp),
                        selectedStyle = themeState.palette,
                        onStyleSelected = {
                            viewModel.setPaletteStyle(it)
                        }
                    )
                }


                ThemePreviewCardButton()
            }

        }
    }
}

@Composable
private fun SettingSwitch(label: String, checked: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onToggle)
    }
}


class ThemeSettingsViewModel(
    private val appSettings: AppSettings
) : ViewModel() {

    private val log = Logger.withTag("ThemeSettingsViewModel")

    init{
        log.i { "ThemeSettingsViewModel" }
    }

    val settingsState =  appSettings.settingsState

    fun setSystemTheme(value: Boolean) {
        viewModelScope.launch {
            appSettings.setSystemTheme(value)
        }
    }

    fun setDarkTheme(isDark: Boolean) {
        viewModelScope.launch {
            appSettings.setDarkTheme(isDark)
        }
    }

    fun setPrimaryColor(color: Color) {
        viewModelScope.launch {
            appSettings.setPrimaryColor(color)
        }
    }
    fun setExpressiveMotion(value: Boolean) {
        viewModelScope.launch {
            appSettings.setExpressiveMotion(value)
        }
    }
    fun setAnimate(value: Boolean) {
        viewModelScope.launch {
            appSettings.setAnimate(value)
        }
    }

    fun setPaletteStyle(paletteStyle: PaletteStyle){
        viewModelScope.launch {
            appSettings.setPaletteStyle(paletteStyle)
        }
    }



}