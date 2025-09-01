package com.eltonkola.everything.ui.screens.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eltonkola.everything.data.GeocodingService
import com.eltonkola.ku.Location
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class PMTilesMapViewModel : ViewModel() {
    private val _state = MutableStateFlow(PMTilesMapState())
    val state: StateFlow<PMTilesMapState> = _state.asStateFlow()

    private var offlineStyleJson: String? = null

    fun setOfflineStyle(styleJson: String) {
        offlineStyleJson = styleJson
    }

    fun setLocation(location: Location) {
        _state.update { currentState ->
            currentState.copy(
                center = LatLng(location.latitude, location.longitude)
            )
        }
    }

    fun toggleOfflineMode() {
        _state.update { currentState ->
            val newOfflineState = !currentState.usePMTilesOffline
            currentState.copy(
                usePMTilesOffline = newOfflineState,
                zoom = if (newOfflineState) {
                    // Switching to offline: limit zoom
                    minOf(currentState.zoom, currentState.maxZoomOffline.toDouble())
                } else {
                    currentState.zoom
                },
                errorMessage = null
            )
        }
    }

    fun showPMTilesInfo() {
        _state.update {
            it.copy(showInfo = true)
        }
    }

    fun hideInfo() {
        _state.update {
            it.copy(showInfo = false)
        }
    }

    fun onMapLoadFinished() {
        _state.update {
            it.copy(
                isMapLoaded = true,
                errorMessage = null
            )
        }
    }

    fun onMapLoadFailed(reason: String?) {
        _state.update {
            it.copy(
                errorMessage = reason ?: "Map failed to load",
                isMapLoaded = false
            )
        }
    }

    fun onError(message: String) {
        _state.update {
            it.copy(errorMessage = message)
        }
    }





    private val geocodingService = GeocodingService()

    private val _uiState = MutableStateFlow(LocationSearchUiState())
    val uiState = _uiState.asStateFlow()

    fun updateQuery(query: String) {
        _uiState.value = _uiState.value.copy(query = query)
    }

    fun searchLocations(query: String, boundingBox: BoundingBoxX) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val results = geocodingService.searchInViewport(query, boundingBox.west, boundingBox.north, boundingBox.east, boundingBox.south)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    results = results
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Search failed"
                )
            }
        }
    }

    fun clearSearch() {
        _uiState.value = LocationSearchUiState()
    }


}
