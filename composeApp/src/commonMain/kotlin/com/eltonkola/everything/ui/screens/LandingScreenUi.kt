package com.eltonkola.everything.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import co.touchlab.kermit.Logger
import com.eltonkola.everything.data.local.AppSettings
import com.eltonkola.everything.ui.Landing
import com.eltonkola.everything.ui.MainApp
import com.eltonkola.everything.ui.popBackStackOrGoTo
import com.eltonkola.everything.ui.restartApp
import everything.composeapp.generated.resources.Res
import everything.composeapp.generated.resources.compose_multiplatform
import everything.composeapp.generated.resources.ic_cyclone
import everything.composeapp.generated.resources.marker
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel


data class TutorialPage(
    val title: String,
    val description: String,
    val imageRes: DrawableResource
)


@Composable
fun LandingScreenUi(
    navController: NavHostController,
    viewModel : LandingViewModel = koinViewModel(),
    ) {
        val pages = listOf(
            TutorialPage(
                title = "Note everything",
                description = "Note everything, like many apps claim.",
                imageRes = Res.drawable.ic_cyclone
            ),
            TutorialPage(
                title = "We mean it",
                description = "not onyl .",
                imageRes = Res.drawable.compose_multiplatform
            ),
            TutorialPage(
                title = "Small Wins, Big Results",
                description = "Daily micro-habits guide you, based on Stanford's Tiny Habits method. Skip the overwhelm—earn rewards for progress, not perfection.",
                imageRes = Res.drawable.marker
            )
        )

        var currentPage by remember { mutableStateOf(0) }

        Scaffold(
            bottomBar = {
                TutorialBottomBar(
                    currentPage = currentPage,
                    totalPages = pages.size,
                    onNext = {
                        if (currentPage < pages.lastIndex) {
                            currentPage++
                        } else {
                            viewModel.onTutorialComplete()
                            navController.restartApp()
                        }
                    },
                    onSkip = {
                        viewModel.onTutorialComplete()
                        navController.restartApp()
                    },
                    showSkip = currentPage < pages.lastIndex
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                Image(
                    painter = painterResource(resource = pages[currentPage].imageRes),
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().aspectRatio(1f)
                )

                Spacer(modifier = Modifier.height(32.dp))
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                ) {
                    Text(
                        text = pages[currentPage].title,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        ),
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = pages[currentPage].description,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = Color.Gray,
                            fontSize = 16.sp
                        ),
                    )

                }

                Spacer(modifier = Modifier.fillMaxWidth().weight(1f))

                DotsIndicator(
                    totalDots = pages.size,
                    selectedIndex = currentPage
                )
            }
        }
    }

    @Composable
    fun TutorialBottomBar(
        currentPage: Int,
        totalPages: Int,
        onNext: () -> Unit,
        onSkip: () -> Unit,
        showSkip: Boolean
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showSkip) {
                TextButton(onClick = onSkip) {
                    Text(text = "Skip", color = MaterialTheme.colorScheme.onSurface)
                }
            } else {
                Spacer(modifier = Modifier.width(64.dp))
            }

            ElevatedButton(
                onClick = onNext,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = if (currentPage == totalPages - 1) "Get Started" else "Next →",
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }

    @Composable
    fun DotsIndicator(
        totalDots: Int,
        selectedIndex: Int
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(totalDots) { index ->
                val color = if (index == selectedIndex) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                }

                Box(
                    modifier = Modifier
                        .padding(6.dp)
                        .size(width = 18.dp, height = 8.dp)
                        .background(
                            color = color,
                            shape = MaterialTheme.shapes.small
                        )
                )
            }
        }
    }


    class LandingViewModel(
        private val appSettings: AppSettings
    ) : ViewModel() {

        private val log = Logger.withTag("LandingViewModel")

        fun onTutorialComplete() {
            log.i { "onTutorialComplete" }
            viewModelScope.launch {
                appSettings.setFirstLaunch(false)
            }
        }
    }