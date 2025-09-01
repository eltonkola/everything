package com.eltonkola.everything.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.eltonkola.everything.data.parser.NoteType
import com.eltonkola.everything.ui.screens.LandingScreenUi
import com.eltonkola.everything.ui.screens.MainScreenUi
import com.eltonkola.everything.ui.screens.SplashScreenUi
import com.eltonkola.everything.ui.screens.main.edit.NoteEditScreen
import kotlinx.serialization.Serializable

@Serializable
object Splash

@Serializable
object Landing

@Serializable
object MainApp

@Serializable
class NoteEdit(val notePath : String?, val noteType: NoteType? = null)

@Composable
fun AppNavigation() {

    val navController = rememberNavController()

    NavHost(
        modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing),
        navController = navController,
        startDestination = Splash
    ) {
        composable<Splash> { SplashScreenUi(navController) }
        composable<Landing> { LandingScreenUi(navController) }
        composable<MainApp> { MainScreenUi(navController) }
        composable<NoteEdit> { backStackEntry ->
            val route: NoteEdit = backStackEntry.toRoute()
            NoteEditScreen(
                filePath = route.notePath,
                noteType = route.noteType,
                navController = navController
            )
        }
    }

}

fun NavController.restartApp() {
    navigate(Splash) {
        popUpTo(graph.id) { inclusive = true }
        launchSingleTop = true
    }
}

fun NavController.popBackStackOrGoTo(route : Any) {
    if (currentBackStack.value.size > 1) {
        popBackStack()
    } else {
        navigate(route) {
            popUpTo(0) { inclusive = true }
            launchSingleTop = true
        }
    }

}