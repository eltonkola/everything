package com.eltonkola.everything.ui.screens.map

import com.eltonkola.everything.data.SearchResult

data class LocationSearchUiState(
    val query: String = "",
    val isLoading: Boolean = false,
    val results: List<SearchResult> = emptyList(),
    val error: String? = null
)
data class LatLng(val latitude: Double, val longitude: Double)

data class PMTilesMapState(
    val center: LatLng = LatLng(20.0, 0.0),
    val zoom: Double = 10.0,
    val usePMTilesOffline: Boolean = false,
    val maxZoomOffline: Float = 7f,
    val showInfo: Boolean = false,
    val pmtilesInfo: PMTilesInfo = PMTilesInfo(),
    val errorMessage: String? = null,
    val isMapLoaded: Boolean = false
)

data class PMTilesInfo(
    val fileSize: String = "Unknown",
    val minZoom: Int = 0,
    val maxZoom: Int = 7,
    val tileCount: String = "Unknown",
    val format: String = "PMTiles",
    val source: String = "composeResources"
)
data class BoundingBoxX(
    val west: Double,
    val north: Double,
    val east: Double,
    val south: Double
)