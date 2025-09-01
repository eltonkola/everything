package com.eltonkola.everything.ui.screens.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.material3.ToggleFloatingActionButtonDefaults.animateIcon
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import co.touchlab.kermit.Logger
import com.composables.icons.lucide.*
import com.eltonkola.everything.data.parser.EvryNote
import com.eltonkola.everything.data.parser.NoteType
import com.eltonkola.everything.data.repository.NotesUseCase
import com.eltonkola.everything.ui.NoteEdit
import com.eltonkola.everything.ui.components.SwipeAction
import com.eltonkola.everything.ui.components.SwipeActionItem
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TabHomeScreenUi(
    navController: NavController,
    viewModel : TabHomeViewModel = koinViewModel(),
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notes") }
            )
        },
        floatingActionButton = {
             val items =
                listOf(
                    Lucide.StickyNote to NoteType.TEXT,
                    Lucide.List to NoteType.TODO,
                    Lucide.Map to NoteType.LOCATION
                )

            var fabMenuExpanded by rememberSaveable { mutableStateOf(false) }

            FloatingActionButtonMenu(
                expanded = fabMenuExpanded,
                button = {
                    ToggleFloatingActionButton(
                        checked = fabMenuExpanded,
                        onCheckedChange = { fabMenuExpanded = !fabMenuExpanded },
                    ) {
                        val imageVector by remember {
                            derivedStateOf {
                                if (checkedProgress > 0.5f) Icons.Filled.Close else Icons.Filled.Add
                            }
                        }
                        Icon(
                            painter = rememberVectorPainter(imageVector),
                            contentDescription = null,
                            modifier = Modifier.animateIcon({ checkedProgress }),
                        )
                    }
                },
            ) {
                items.forEachIndexed { i, item ->
                    FloatingActionButtonMenuItem(
                        modifier =
                            Modifier.semantics {
                                isTraversalGroup = true
                                if (i == items.size - 1) {
                                    customActions =
                                        listOf(
                                            CustomAccessibilityAction(
                                                label = "Close menu",
                                                action = {
                                                    true
                                                },
                                            )
                                        )
                                }
                            },
                        onClick = {
                            navController.navigate(NoteEdit(null, item.second))
                            fabMenuExpanded = false
                        },
                        icon = { Icon(item.first, contentDescription = null) },
                        text = { Text(text = item.second.title) },
                    )
                }
            }

        }
    ) {

        val notes by viewModel.allNotes.collectAsStateWithLifecycle(emptyList())

        LazyColumn(
            modifier = Modifier.padding(it)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            items(notes){ note ->

                SwipeActionItem(
                    modifier = Modifier.padding(vertical = 1.dp),
                    leftActions = listOf(
                        SwipeAction(
                            icon = if (note.commonFields.archived) Lucide.ArchiveX else Lucide.Archive,
                            label = if (note.commonFields.archived) "Unarchive" else "Archive",
                            backgroundColor = if (note.commonFields.archived) Color(0xFF757575) else Color(0xFF4CAF50),
                            onAction = {
                                viewModel.archiveUnArchiveNote(note)
                            }
                        )
                    ),
                    rightActions = listOf(
                        SwipeAction(
                            icon = Lucide.Pencil,
                            label = "Edit",
                            backgroundColor = Color(0xFF2196F3),
                            onAction = {
                                navController.navigate(NoteEdit(note.filePath))
                            }
                        ),
                        SwipeAction(
                            icon = Lucide.Trash2,
                            label = "Delete",
                            backgroundColor = Color(0xFFF44336),
                            onAction = {
                                viewModel.deleteNote(note)
                            }
                        )
                    )
                ) {
                    EvryNoteCard(
                        note = note,
                        onClick = {
                            navController.navigate(NoteEdit(note.filePath))
                        },
                        onLongClick = {
                            viewModel.deleteNote(note)
                        }
                    )
                }
            }


        }
    }
}

class TabHomeViewModel(
    private val notesUseCase: NotesUseCase
) : ViewModel() {
    fun deleteNote(note: EvryNote) {
        viewModelScope.launch {
            notesUseCase.deleteNote(note.filePath)
        }
    }

    private val log = Logger.withTag("TabHomeViewModel")

    val allNotes = notesUseCase.notes

    init{
        log.i { "TabHomeViewModel" }

        viewModelScope.launch {
            notesUseCase.refreshNotes()
        }
    }
    @OptIn(ExperimentalTime::class)
    fun archiveUnArchiveNote(note: EvryNote) {
        viewModelScope.launch {
            try {
                notesUseCase.loadNote(note.filePath)
                    .onSuccess { editableNote ->
                        val updatedNote = editableNote.copy(
                            archived = !editableNote.archived,
                            edited = Clock.System.now().toString()
                        )
                        notesUseCase.updateNote(updatedNote)
                            .onFailure { error ->
                                log.e { "Failed to archive/unarchive note: ${error.message}" }
                            }
                    }
                    .onFailure { error ->
                        log.e { "Failed to load note for archiving: ${error.message}" }
                    }
            } catch (e: Exception) {
                log.e(e) { "Unexpected error archiving/unarchiving note" }
            }
        }
    }

}