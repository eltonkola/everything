package com.eltonkola.everything

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.eltonkola.everything.di.appModule
import com.eltonkola.everything.di.desktopModule
import com.eltonkola.everything.di.initKoin
import dev.sargunv.maplibrecompose.compose.KcefProvider
import dev.sargunv.maplibrecompose.compose.MaplibreContextProvider

fun main() = application {

    initKoin {
        modules(appModule, desktopModule)
    }

    Window(
        onCloseRequest = ::exitApplication,
        title = "Everything",
    ) {
        //App()


        KcefProvider (
            loading = {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ){
                    CircularProgressIndicator()
                }
            },
            content = { MaplibreContextProvider { App() } },
        )


    }



}