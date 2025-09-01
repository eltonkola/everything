package com.eltonkola.everything.ui.screens.main.settings


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import co.touchlab.kermit.Logger
import com.composables.icons.lucide.ChevronLeft
import com.composables.icons.lucide.Github
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Palette
import com.eltonkola.everything.ui.screens.main.settings.theme.ThemePreviewCardButton
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainSettingsScreenUi(
    navController: NavController,
    viewModel : MainSettingsViewModel = koinViewModel(),
) {

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("App Preferences") }
            )
        }
    ) {
        Column(
            modifier = Modifier.padding(it)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                "App Settings",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth()
            )

            SettingsButton(
                onClick = { navController.navigate(SettingsTheme) },
                icon = Lucide.Palette,
                title = "Preview Theme",
                subtitle = "See how your theme looks on ui elements"
            )

            Spacer(modifier = Modifier.weight(1f))


            ElevatedButton(
                onClick = { navController.popBackStack() }
            ){
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ){
                    Icon(Lucide.Github, contentDescription = "github")
                    Spacer(modifier = Modifier.size(4.dp))
                    Text("Github - check the source code", style = MaterialTheme.typography.bodyMedium)
                }
            }
            Text(
                "Version 1.0.0",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                "Built with ❤\uFE0F by Elton Kola",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.size(16.dp))

        }
    }
}



class MainSettingsViewModel(

) : ViewModel() {

    private val log = Logger.withTag("MainSettingsViewModel")

    init{
        log.i { "MainSettingsViewModel" }
    }



}