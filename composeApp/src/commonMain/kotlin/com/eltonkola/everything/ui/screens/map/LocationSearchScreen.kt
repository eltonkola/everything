package com.eltonkola.everything.ui.screens.map


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import kotlin.math.pow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.List
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Map
import com.eltonkola.everything.data.SearchResult
import dev.sargunv.maplibrecompose.compose.CameraState


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationSearchScreen(
    cameraPositionState: CameraState,
    uiState: LocationSearchUiState,
    onQueryChange: (String) -> Unit,
    onSearch: (String, BoundingBoxX) -> Unit,
    onLocationSelected: (SearchResult) -> Unit,
    onClearSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    var showlist by remember { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(start = 16.dp, top = 32.dp, end = 16.dp, bottom = 16.dp)
    ) {
        // Search Bar
        SearchBar(
            modifier = Modifier.fillMaxWidth(),
            query = uiState.query,
            onQueryChange = onQueryChange,
            onSearch = { query ->

                val bounds = cameraPositionState.position?.let { pos ->
                    // Calculate approximate bounds based on zoom level
                    val zoomFactor = 2.0.pow(pos.zoom - 10.0)
                    val latOffset = 0.5 / zoomFactor
                    val lonOffset = 0.5 / zoomFactor

                    val center = pos.target
                    BoundingBoxX(
                        west = center.longitude - lonOffset,
                        north = center.latitude + latOffset,
                        east = center.longitude + lonOffset,
                        south = center.latitude - latOffset
                    )
                }

                bounds?.let {
                    onSearch(query, it )
                }



//                onSearch(query)
                keyboardController?.hide()
            },
            onClear = onClearSearch,
        )

        Spacer(modifier = Modifier.height(16.dp))


        // Content Area
        when {
            uiState.isLoading -> {
                LoadingState(
                    modifier = Modifier.fillMaxSize()
                )
            }

            uiState.error != null -> {
                ErrorState(
                    error = uiState.error,
                    onRetry = {

                        val bounds = cameraPositionState.position?.let { pos ->
                            // Calculate approximate bounds based on zoom level
                            val zoomFactor = 2.0.pow(pos.zoom - 10.0)
                            val latOffset = 0.5 / zoomFactor
                            val lonOffset = 0.5 / zoomFactor

                            val center = pos.target
                            BoundingBoxX(
                                west = center.longitude - lonOffset,
                                north = center.latitude + latOffset,
                                east = center.longitude + lonOffset,
                                south = center.latitude - latOffset
                            )
                        }

                        bounds?.let {
                            onSearch(uiState.query, it )
                        }


                              },
                    modifier = Modifier.fillMaxSize()
                )
            }

            uiState.results.isNotEmpty() -> {
                if(showlist) {

                    IconButton(onClick = { showlist = !showlist }) {
                        Icon(if(showlist) Lucide.Map else Lucide.List, contentDescription = "Show List/Map")
                    }

                    SearchResults(
                        results = uiState.results,
                        onLocationSelected = onLocationSelected,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            uiState.query.isNotBlank() -> {
                EmptyState(
                    message = "No locations found for \"${uiState.query}\"",
                    modifier = Modifier.fillMaxSize()
                )
            }

            else -> {

            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        placeholder = {
            Text(
                text = "Search locations...",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        },
        trailingIcon = {
            if (query.isNotBlank()) {
                IconButton(onClick = onClear) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear search",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        },
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(
            onSearch = { onSearch(query) }
        ),
        singleLine = true,
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
private fun LoadingState(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Searching locations...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun ErrorState(
    error: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Search failed",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Retry")
            }
        }
    }
}

@Composable
private fun EmptyState(
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun SearchResults(
    results: List<SearchResult>,
    onLocationSelected: (SearchResult) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = results,
            key = { "${it.lat}_${it.lon}_${it.displayName}" }
        ) { result ->
            LocationResultItem(
                result = result,
                onClick = { onLocationSelected(result) }
            )
        }
    }
}

@Composable
private fun LocationResultItem(
    result: SearchResult,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Location Icon
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Location Details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = result.displayName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (result.type != null || result.placeClass != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = result.type?.replaceFirstChar { it.uppercase() }
                            ?: result.placeClass?.replaceFirstChar { it.uppercase() }
                            ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${result.latitude}, ${result.longitude}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    fontSize = 11.sp
                )
            }
        }
    }
}

// Data classes



