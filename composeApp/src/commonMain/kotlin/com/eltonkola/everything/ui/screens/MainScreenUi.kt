package com.eltonkola.everything.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import co.touchlab.kermit.Logger
import com.composables.icons.lucide.*
import com.eltonkola.everything.ui.screens.main.TabFilesScreenUi
import com.eltonkola.everything.ui.screens.main.TabHomeScreenUi
import com.eltonkola.everything.ui.screens.main.TabMapScreenUi
import com.eltonkola.everything.ui.screens.main.settings.TabSettingsScreenUi
import org.koin.compose.viewmodel.koinViewModel

data class TabItem(val title: String, val icon: ImageVector)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenUi(
    navController: NavController,
    viewModel : MainAppViewModel = koinViewModel(),
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf(
        TabItem("Home", Lucide.NotebookText),
        TabItem("Map", Lucide.Map),
        TabItem("Files", Lucide.FolderDot),
        TabItem("Settings", Lucide.Settings)
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        icon = { Icon(tab.icon, contentDescription = tab.title) },
                        label = { Text(tab.title) },
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                    )
                }
            }
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (selectedTab) {
                0 -> TabHomeScreenUi(navController)
                1 -> TabMapScreenUi(navController)
                2 -> TabFilesScreenUi(navController)
                3 -> TabSettingsScreenUi(navController)
            }
        }
    }
}

class MainAppViewModel(

) : ViewModel() {

    private val log = Logger.withTag("MainAppViewModel")

    init{
        log.i { "MainAppViewModel" }
    }
}