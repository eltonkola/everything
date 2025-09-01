package com.eltonkola.everything.ui.screens.map

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
 fun PMTilesControlPanel(
    state: PMTilesMapState,
    onToggleOffline: () -> Unit,
    onShowInfo: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                onClick = onToggleOffline,
                label = {
                    Text(if (state.usePMTilesOffline) "PMTiles" else "Online")
                },
                selected = state.usePMTilesOffline,
                leadingIcon = {
                    Icon(
                        imageVector = if (state.usePMTilesOffline)
                            Icons.Default.Storage else Icons.Default.Cloud,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            )

            AssistChip(
                onClick = onShowInfo,
                label = { Text("Info") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            )

            AssistChip(
                onClick = { },
                label = { Text("Z${state.zoom.toInt()}") },
                leadingIcon = {
                    Icon(
                        Icons.Default.ZoomIn,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            )
        }
    }
}

@Composable
fun PMTilesInfoCard(
    pmtilesInfo: PMTilesInfo,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "PMTiles Info",
                    style = MaterialTheme.typography.headlineSmall
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            HorizontalDivider()

            InfoRow("File Size", pmtilesInfo.fileSize)
            InfoRow("Zoom Range", "${pmtilesInfo.minZoom} - ${pmtilesInfo.maxZoom}")
            InfoRow("Tile Count", pmtilesInfo.tileCount)
            InfoRow("Format", pmtilesInfo.format)
            InfoRow("Source", pmtilesInfo.source)
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
