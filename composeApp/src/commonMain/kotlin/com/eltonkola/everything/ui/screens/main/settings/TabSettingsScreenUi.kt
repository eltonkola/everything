package com.eltonkola.everything.ui.screens.main.settings


import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.eltonkola.everything.ui.screens.main.settings.theme.ThemeSettingsScreenUi
import kotlinx.serialization.Serializable


@Serializable
object SettingsMain

@Serializable
object SettingsTheme


@Composable
fun TabSettingsScreenUi(
    parentNavController: NavController,
) {
    val navController = rememberNavController()

    NavHost(
        modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing),
        navController = navController,
        startDestination = SettingsMain
    ) {
        composable<SettingsMain> { MainSettingsScreenUi(navController) }
        composable<SettingsTheme> { ThemeSettingsScreenUi(navController) }
    }
}
