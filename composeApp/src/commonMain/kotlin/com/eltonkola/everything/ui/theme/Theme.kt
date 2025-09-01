package com.eltonkola.everything.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import com.eltonkola.everything.data.local.AppSettings
import com.materialkolor.DynamicMaterialExpressiveTheme
import com.materialkolor.DynamicMaterialTheme
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import com.materialkolor.rememberDynamicColorScheme
import org.koin.compose.koinInject


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun AppTheme(
    appSettings: AppSettings = koinInject(),
    content: @Composable () -> Unit
) {
    val themeState by appSettings.settingsState.collectAsState()

    val isSystemInDarkTheme = isSystemInDarkTheme()
    val isDark by remember(themeState) {
        derivedStateOf {
            if (themeState.system) {
                isSystemInDarkTheme
            } else {
                themeState.dark
            }
        }
    }

//    val colorScheme = rememberDynamicColorScheme(
//        seedColor = seedColor,
//        isDark = isDark,
//        specVersion = ColorSpec.SpecVersion.SPEC_2025,
//        style = PaletteStyle.Expressive,
//    )


//        MaterialTheme(
//            typography = TekoTypography(),
//            shapes = AppShapes,
//            colorScheme = colorScheme,
//            content = { Surface(content = content) }
//        )

//    DynamicMaterialTheme(
//        seedColor = themeState.primaryColor,
//        isDark = isDark,
//        animate = themeState.animate,
//        content = content,
//    )

    DynamicMaterialExpressiveTheme(
        seedColor = themeState.primaryColor,
        motionScheme =  if(themeState.expressiveMotion) MotionScheme.expressive() else MotionScheme.standard(),
        isDark = isDark,
        animate = themeState.animate,
        content = content,
        style = themeState.palette,
    )

}

@Composable
internal expect fun SystemAppearance(isDark: Boolean)
