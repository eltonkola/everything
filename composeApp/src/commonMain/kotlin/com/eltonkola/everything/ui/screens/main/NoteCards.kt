package com.eltonkola.everything.ui.screens.main

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.eltonkola.everything.data.parser.EvryNote

@Composable
fun EvryNoteCard(
    note: EvryNote,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if(note.commonFields.archived) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with note type icon and title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Note type icon
                Icon(
                    imageVector = when (note) {
                        is EvryNote.TextNote -> Icons.Default.Description
                        is EvryNote.TodoNote -> Icons.Default.CheckBox
                        is EvryNote.LocationNote -> Icons.Default.LocationOn
                    },
                    contentDescription = null,
                    tint = when (note) {
                        is EvryNote.TextNote -> MaterialTheme.colorScheme.primary
                        is EvryNote.TodoNote -> MaterialTheme.colorScheme.secondary
                        is EvryNote.LocationNote -> MaterialTheme.colorScheme.tertiary
                    },
                    modifier = Modifier.size(20.dp)
                )

                // Note title
                Text(
                    text = note.commonFields.title ?:  "Untitled" ,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                // Favorite/Pin indicator
//                if (note.commonFields.isPinned) {
//                    Icon(
//                        imageVector = Icons.Default.PushPin,
//                        contentDescription = "Pinned",
//                        tint = MaterialTheme.colorScheme.primary,
//                        modifier = Modifier.size(16.dp)
//                    )
//                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Note type specific content preview
            when (note) {
                is EvryNote.TextNote -> {
                    TextNotePreview(note = note)
                }
                is EvryNote.TodoNote -> {
                    //TodoNotePreview(note = note)
                }
                is EvryNote.LocationNote -> {
                   // LocationNotePreview(note = note)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom row with tags and date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                // Tags
                if (note.commonFields.tags.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(note.commonFields.tags.take(3)) { tag ->
                            AssistChip(
                                onClick = { },
                                label = {
                                    Text(
                                        text = tag,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                modifier = Modifier.height(24.dp)
                            )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }

                // Last modified date
                Text(
                    text = formatDate(note.commonFields.edited),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

private fun formatDate(date: String?): String {
    //TODO format date
    return date ?: ""
}

// Text Note Preview
@Composable
private fun TextNotePreview(note: EvryNote.TextNote) {
    if (note.content.isNotEmpty()) {
        Text(
            text = note.content.take(150), // Preview first 150 chars
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// Todo Note Preview
//@Composable
//private fun TodoNotePreview(note: EvryNote.TodoNote) {
//    Column {
//        // Show todo stats
//        Row(
//            horizontalArrangement = Arrangement.spacedBy(16.dp),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            val completedCount = note.todoFields.todos.count { it.isCompleted }
//            val totalCount = note.todoFields.todos.size
//
//            Row(
//                horizontalArrangement = Arrangement.spacedBy(4.dp),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Icon(
//                    imageVector = Icons.Default.CheckCircle,
//                    contentDescription = null,
//                    tint = MaterialTheme.colorScheme.secondary,
//                    modifier = Modifier.size(16.dp)
//                )
//                Text(
//                    text = "$completedCount/$totalCount completed",
//                    style = MaterialTheme.typography.bodySmall,
//                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
//                )
//            }
//
//            if (note.todoFields.dueDate != null) {
//                Row(
//                    horizontalArrangement = Arrangement.spacedBy(4.dp),
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Icon(
//                        imageVector = Icons.Default.Schedule,
//                        contentDescription = null,
//                        tint = MaterialTheme.colorScheme.secondary,
//                        modifier = Modifier.size(16.dp)
//                    )
//                    Text(
//                        text = formatDate(note.todoFields.dueDate),
//                        style = MaterialTheme.typography.bodySmall,
//                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
//                    )
//                }
//            }
//        }
//
//        // Show content preview if exists
//        if (note.content.isNotEmpty()) {
//            Spacer(modifier = Modifier.height(8.dp))
//            Text(
//                text = note.content.take(100),
//                style = MaterialTheme.typography.bodySmall,
//                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
//                maxLines = 2,
//                overflow = TextOverflow.Ellipsis
//            )
//        }
//    }
//}

// Location Note Preview
//@Composable
//private fun LocationNotePreview(note: EvryNote.LocationNote) {
//    Column {
//        // Location info
//        Row(
//            horizontalArrangement = Arrangement.spacedBy(8.dp),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Icon(
//                imageVector = Icons.Default.Place,
//                contentDescription = null,
//                tint = MaterialTheme.colorScheme.tertiary,
//                modifier = Modifier.size(16.dp)
//            )
//
//            Column {
//                if (note.locationFields.address.isNotEmpty()) {
//                    Text(
//                        text = note.locationFields.address,
//                        style = MaterialTheme.typography.bodySmall,
//                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
//                        maxLines = 1,
//                        overflow = TextOverflow.Ellipsis
//                    )
//                } else {
//                    Text(
//                        text = "${note.locationFields.latitude}, ${note.locationFields.longitude}",
//                        style = MaterialTheme.typography.bodySmall,
//                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
//                    )
//                }
//            }
//        }
//
//        // Show content preview if exists
//        if (note.content.isNotEmpty()) {
//            Spacer(modifier = Modifier.height(8.dp))
//            Text(
//                text = note.content.take(100),
//                style = MaterialTheme.typography.bodySmall,
//                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
//                maxLines = 2,
//                overflow = TextOverflow.Ellipsis
//            )
//        }
//    }
//}
