package com.eltonkola.everything.ui.screens.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eltonkola.ku.Location
import dev.sargunv.maplibrecompose.compose.ClickResult
import dev.sargunv.maplibrecompose.compose.MaplibreMap
import dev.sargunv.maplibrecompose.compose.layer.SymbolLayer
import dev.sargunv.maplibrecompose.compose.rememberCameraState
import dev.sargunv.maplibrecompose.core.BaseStyle
import dev.sargunv.maplibrecompose.core.CameraPosition
import dev.sargunv.maplibrecompose.core.source.ComputedSource
import dev.sargunv.maplibrecompose.core.source.ComputedSourceOptions
import dev.sargunv.maplibrecompose.expressions.dsl.image
import everything.composeapp.generated.resources.Res
import everything.composeapp.generated.resources.marker
import io.github.dellisd.spatialk.geojson.BoundingBox
import io.github.dellisd.spatialk.geojson.Feature
import io.github.dellisd.spatialk.geojson.FeatureCollection
import io.github.dellisd.spatialk.geojson.Point
import io.github.dellisd.spatialk.geojson.Position
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.InternalResourceApi
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.readResourceBytes
import kotlin.time.Duration.Companion.seconds


@OptIn(ExperimentalResourceApi::class, InternalResourceApi::class)
@Composable
fun PMTilesMapScreen(modifier: Modifier = Modifier, location: Location) {
    val viewModel: PMTilesMapViewModel = viewModel()
    val state by viewModel.state.collectAsState()
    val scope = rememberCoroutineScope()

    var offlineStyle by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val styleResource = readResourceBytes("files/offline_style.json")
                offlineStyle = styleResource.decodeToString()
                viewModel.setOfflineStyle(offlineStyle!!)
            } catch (e: Exception) {
                viewModel.onError("Failed to load offline style: ${e.message}")
            }
        }
        viewModel.setLocation(location)
    }

    val camera = rememberCameraState(
        firstPosition = CameraPosition(
//                    target = Position(state.center.longitude, state.center.latitude),
            target = Position(location.longitude, location.latitude),
            zoom = state.zoom,
            bearing = 0.0,
            tilt = 0.0
        )
    )

//    var selectedFeature by mutableStateOf<Feature?>(null)
    val uiState by viewModel.uiState.collectAsState()


    Box(modifier = modifier.fillMaxSize()) {
        MaplibreMap(
            modifier = Modifier.fillMaxSize(),
            baseStyle = when {
                state.usePMTilesOffline && offlineStyle != null -> {
                    BaseStyle.Json(offlineStyle!!)
                }

                else -> {
                    BaseStyle.Uri("https://tiles.openfreemap.org/styles/liberty")
                }
            },
            cameraState = camera,
            zoomRange = if (state.usePMTilesOffline) 0f..state.maxZoomOffline else 0f..20f,
            onMapLoadFinished = {
                viewModel.onMapLoadFinished()
            },
            onMapLoadFailed = { reason ->
                viewModel.onMapLoadFailed(reason)
            },
            onMapClick = { pos, offset ->
                val features = camera.projection?.queryRenderedFeatures(offset)
                if (!features.isNullOrEmpty()) {
                    println("Clicked on ${features[0].json()}")
                    ClickResult.Consume


                } else {
                    ClickResult.Pass
                }
            },
            onMapLongClick = { pos, offset ->
                println("Long click at $pos")
                ClickResult.Pass
            },
        ) {

            val marker = painterResource(Res.drawable.marker)

            val results = remember {
                ComputedSource(
                    id = "search_result",
                    options = ComputedSourceOptions(),
                    getFeatures = { bounds: BoundingBox, zoomLevel: Int ->

                        FeatureCollection(
                            features = uiState.results.map {
                                Feature(
                                    geometry = Point(Position(it.longitude, it.latitude)),
                                    properties = emptyMap(),
                                    id = null,
                                    bbox = null
                                )
                            },
                            bbox = null
                        )
                    }
                )
            }

            SymbolLayer(
                id = "search_result_markers",
                source = results,
                onClick = { features ->
                    // selectedFeature = features.firstOrNull()
                    ClickResult.Consume
                },
                iconImage = image(marker),
            )


        }

        // Control Panel
        PMTilesControlPanel(
            state = state,
            onToggleOffline = viewModel::toggleOfflineMode,
            onShowInfo = viewModel::showPMTilesInfo,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        )

        // Info card
        if (state.showInfo) {
            PMTilesInfoCard(
                pmtilesInfo = state.pmtilesInfo,
                onDismiss = viewModel::hideInfo,
                modifier = Modifier.align(Alignment.BottomStart)
            )
        }

        // Error display
        state.errorMessage?.let { error ->
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = error,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }




        LocationSearchScreen(
            cameraPositionState = camera,
            uiState = uiState,
            onQueryChange = viewModel::updateQuery,
            onSearch = viewModel::searchLocations,
            onLocationSelected = { result ->
                println("Selected: ${result.displayName} at ${result.latitude}, ${result.longitude}")

                scope.launch {
                    camera.animateTo(
                        finalPosition =
                            camera.position.copy(target = Position(
                                latitude = result.latitude,
                                longitude = result.longitude
                            )
                            )
                        ,
                        duration = 3.seconds,
                    )
                }

            },
            onClearSearch = viewModel::clearSearch
        )


    }
}
