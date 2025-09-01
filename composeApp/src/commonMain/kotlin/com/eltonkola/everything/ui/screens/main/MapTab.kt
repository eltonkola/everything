package com.eltonkola.everything.ui.screens.main


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import co.touchlab.kermit.Logger
import com.eltonkola.everything.ui.screens.map.PMTilesMapScreen
import com.eltonkola.ku.LocationProvider
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabMapScreenUi(
    navController: NavController,
    viewModel : TabMapViewModel = koinViewModel(),
) {
    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text("Maps") }
//            )
//        }
    ) {
        Box(
            modifier = Modifier.padding(it)
                .fillMaxSize(),
        ) {

            LocationProvider(
                onLocationReceived = { location ->
                    PMTilesMapScreen(modifier = Modifier.fillMaxSize(), location)
                },
                onInitial = { onRequestLocation ->
                    LaunchedEffect(Unit){
                        onRequestLocation()
                    }
                }
            )

        }
    }
}

class TabMapViewModel(

) : ViewModel() {

    private val log = Logger.withTag("TabMapViewModel")

    init{
        log.i { "TabMapViewModel" }
    }
}