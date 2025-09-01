package com.eltonkola.everything.data.local

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.materialkolor.PaletteStyle
import dev.sargunv.maplibrecompose.expressions.ast.NullLiteral.value
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*

data class SettingsState(
    val system: Boolean,
    val dark: Boolean,
    val firstLaunch: Boolean,
    val primaryColor: Color,
    val expressiveMotion: Boolean,
    val animate: Boolean,
    val palette: PaletteStyle
)

class AppSettings(
    private val dataStore: DataStore<Preferences>,

    ) {

    private object PreferencesKeys {
        val DARK_THEME = booleanPreferencesKey("dark_theme")
        val SYSTEM_THEME = booleanPreferencesKey("system_theme")
        val FIRST_LAUNCH = booleanPreferencesKey("first_launch")

        val PRIMARY_COLOR = intPreferencesKey("primary_color")

        val EXPRESSIVE_MOTION = booleanPreferencesKey("expressive_motion")
        val ANIMATE = booleanPreferencesKey("animate")
        val PALETTE = stringPreferencesKey("palette")
    }

    val settingsState: StateFlow<SettingsState> = dataStore.data
        .map { preferences ->
            SettingsState(
                system = preferences[PreferencesKeys.SYSTEM_THEME] ?: true,
                dark = preferences[PreferencesKeys.DARK_THEME] ?: false,
                firstLaunch = preferences[PreferencesKeys.FIRST_LAUNCH] ?: true,
                primaryColor = Color(preferences[PreferencesKeys.PRIMARY_COLOR] ?: Color.Blue.toArgb()),
                expressiveMotion = preferences[PreferencesKeys.EXPRESSIVE_MOTION] ?: true,
                animate = preferences[PreferencesKeys.ANIMATE] ?: true,
                palette = preferences[PreferencesKeys.PALETTE]?.toPalette() ?: PaletteStyle.Expressive
            )
        }
        .stateIn(
            scope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SettingsState(
                system = true,
                dark = false,
                firstLaunch = true,
                primaryColor = Color.Blue,
                expressiveMotion = true,
                animate = true,
                palette = PaletteStyle.Expressive
            )
        )

    fun String.toPalette() : PaletteStyle {
        return try {
            enumValueOf<PaletteStyle>(this)
        } catch (e: IllegalArgumentException) {
            PaletteStyle.Expressive
        }
    }

    suspend fun setDarkTheme(isDark: Boolean) {
        dataStore.edit { settings ->
            settings[PreferencesKeys.DARK_THEME] = isDark
        }
    }

    suspend fun setSystemTheme(value: Boolean) {
        dataStore.edit { settings ->
            settings[PreferencesKeys.SYSTEM_THEME] = value
        }
    }

    suspend fun isFirstLaunch(): Boolean {
        return dataStore.data.map { it[PreferencesKeys.FIRST_LAUNCH] ?: true }.first()
    }

    suspend fun setFirstLaunch(value: Boolean) {
        dataStore.edit { settings ->
            settings[PreferencesKeys.FIRST_LAUNCH] = value
        }
    }

    suspend fun setPrimaryColor(color: Color) {
        dataStore.edit { settings ->
            settings[PreferencesKeys.PRIMARY_COLOR] = color.toArgb()
        }
    }

    suspend fun setExpressiveMotion(value: Boolean) {
        dataStore.edit { settings ->
            settings[PreferencesKeys.EXPRESSIVE_MOTION] = value
        }
    }

    suspend fun setAnimate(value: Boolean) {
        dataStore.edit { settings ->
            settings[PreferencesKeys.ANIMATE] = value
        }
    }

    suspend fun setPaletteStyle(palette: PaletteStyle) {
        dataStore.edit { settings ->
            settings[PreferencesKeys.PALETTE] = palette.name
        }
    }


}