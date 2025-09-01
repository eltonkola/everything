package com.eltonkola.everything.ui.screens.main


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import co.touchlab.kermit.Logger
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabFilesScreenUi(
    navController: NavController,
    viewModel : TabFilesViewModel = koinViewModel(),
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Files") }
            )
        }
    ) {
        Column(
            modifier = Modifier.padding(it)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text(
                "Files Tab Screen",
                style = MaterialTheme.typography.titleMedium
            )

        }
    }
}

class TabFilesViewModel(

) : ViewModel() {

    private val log = Logger.withTag("TabFilesViewModel")

    init{
        log.i { "TabFilesViewModel" }
    }
}