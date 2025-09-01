package com.eltonkola.everything.ui.screens


import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import co.touchlab.kermit.Logger
import com.eltonkola.everything.data.local.AppSettings
import com.eltonkola.everything.ui.Landing
import com.eltonkola.everything.ui.MainApp
import com.eltonkola.everything.ui.Splash
import everything.composeapp.generated.resources.Res
import everything.composeapp.generated.resources.logo_everything
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel


@Composable
fun SplashScreenUi(
    navController: NavController,
    viewModel : SplashViewModel = koinViewModel(),
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {

        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        LaunchedEffect(Unit) {
            viewModel.splash()
        }

        LaunchedEffect(uiState) {
            when (uiState) {
                is SplashUiState.Loading -> {

                }
                is SplashUiState.FirstTime -> {
                    navController.navigate(Landing) {
                        popUpTo(Splash) { inclusive = true }
                    }
                }
                is SplashUiState.Ready -> {
                    navController.navigate(MainApp) {
                        popUpTo(0) {
                            inclusive = true
                        }
                        launchSingleTop = true
                        restoreState = false
                    }
                }
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Image(
                painter = painterResource(Res.drawable.logo_everything),
                contentDescription = "Logo",
                modifier = Modifier.size(140.dp)
            )
            Spacer(modifier = Modifier.size(16.dp))

            Text(
                text = "Everything",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            )

            Spacer(modifier = Modifier.size(16.dp))

            CircularProgressIndicator()


        }
    }

}

sealed class SplashUiState {
    data object Loading : SplashUiState()
    data object FirstTime : SplashUiState()
    data object Ready : SplashUiState()
}

class SplashViewModel(
    private val appSettings: AppSettings
) : ViewModel() {

    private val log = Logger.withTag("SplashViewModel")

    private val _uiState = MutableStateFlow<SplashUiState>(SplashUiState.Loading)
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    fun splash() {
        log.i { "splash called" }
        viewModelScope.launch {
                log.i { "Starting splash flow" }
                _uiState.value = SplashUiState.Loading

                log.i { "Checking first launch status" }
                val isFirstLaunch = appSettings.isFirstLaunch()

                if (isFirstLaunch) {
                    log.i { "First launch detected" }
                    _uiState.value = SplashUiState.FirstTime
                    return@launch
                }else {
                    log.i { "Ready to go" }
                    _uiState.value = SplashUiState.Ready
                }
        }
    }

}