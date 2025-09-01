package com.eltonkola.everything.ui.screens.main.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.eltonkola.everything.data.local.EditableLocationFields
import com.eltonkola.everything.data.local.EditableNote
import com.eltonkola.everything.data.local.EditableTodoFields
import com.eltonkola.everything.data.parser.NoteType
import com.eltonkola.everything.data.repository.NotesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
class NoteEditViewModel(
    private val notesUseCase: NotesUseCase
) : ViewModel() {

    private val log = Logger.withTag("NoteEditViewModel")

    private val _uiState = MutableStateFlow(NoteEditUiState())
    val uiState: StateFlow<NoteEditUiState> = _uiState.asStateFlow()

    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState: StateFlow<SaveState> = _saveState.asStateFlow()

    @OptIn(ExperimentalTime::class)
    fun loadNote(filePath: String?) {
        if (filePath.isNullOrBlank()) {
            // Create new note
            _uiState.value = _uiState.value.copy(
                note = EditableNote(
                    created = Clock.System.now().toString(),
                    type = NoteType.TEXT,
                    title = "",
                    content = "",
                    tags = emptyList(),
                    todoFields = EditableTodoFields(),
                    locationFields = EditableLocationFields(),
                    filePath = ""
                ),
                isLoading = false,
                isNewNote = true
            )
            log.i { "Created new note template" }
        } else {
            // Load existing note
            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)

                try {
                    notesUseCase.loadNote(filePath)
                        .onSuccess { note ->
                            _uiState.value = _uiState.value.copy(
                                note = note.copy(filePath = filePath),
                                isLoading = false,
                                isNewNote = false,
                                error = null
                            )
                            log.i { "Successfully loaded existing note from $filePath, type: ${note.type}" }
                        }
                        .onFailure { error ->
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = error.message ?: "Failed to load note"
                            )
                            log.e { "Error loading note from $filePath: ${error.message}" }
                        }
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Unexpected error loading note: ${e.message}"
                    )
                    log.e(e) { "Unexpected error loading note from $filePath" }
                }
            }
        }
    }

    fun updateTitle(title: String) {
        _uiState.value = _uiState.value.copy(
            note = _uiState.value.note.copy(title = title)
        )
        log.d { "Updated title: $title" }
    }

    fun updateContent(content: String) {
        _uiState.value = _uiState.value.copy(
            note = _uiState.value.note.copy(content = content)
        )
        log.d { "Updated content (${content.length} chars)" }
    }

    fun updateTags(tags: List<String>) {
        val cleanTags = tags.distinct().filter { it.isNotBlank() }
        _uiState.value = _uiState.value.copy(
            note = _uiState.value.note.copy(tags = cleanTags)
        )
        log.d { "Updated tags: $cleanTags" }
    }

    fun updateType(type: NoteType) {
        val currentNote = _uiState.value.note
        if (currentNote.type != type) {
            _uiState.value = _uiState.value.copy(
                note = currentNote.copy(type = type)
            )
            log.i { "Updated note type to: $type" }
        }
    }

    fun updateTodoFields(fields: EditableTodoFields) {
        _uiState.value = _uiState.value.copy(
            note = _uiState.value.note.copy(todoFields = fields)
        )
        log.d { "Updated todo fields: priority=${fields.priority}, due=${fields.due}" }
    }

    fun updateLocationFields(fields: EditableLocationFields) {
        _uiState.value = _uiState.value.copy(
            note = _uiState.value.note.copy(locationFields = fields)
        )
        log.d { "Updated location fields: address=${fields.address}" }
    }

    @OptIn(ExperimentalTime::class)
    fun saveNote() {
        val currentNote = _uiState.value.note

        // Basic validation
        val hasContent = when (currentNote.type) {
            NoteType.TODO -> currentNote.content.isNotBlank() || currentNote.title.isNotBlank()
            NoteType.LOCATION -> currentNote.title.isNotBlank() || currentNote.content.isNotBlank() ||
                    currentNote.locationFields.address.isNotBlank()
            else -> currentNote.title.isNotBlank() || currentNote.content.isNotBlank()
        }

        if (!hasContent) {
            val errorMsg = when (currentNote.type) {
                NoteType.TODO -> "Todo list must have a title or items"
                NoteType.LOCATION -> "Location note must have a name, address, or notes"
                else -> "Note must have a title or content"
            }
            _saveState.value = SaveState.Error(errorMsg)
            log.w { "Save validation failed: $errorMsg" }
            return
        }

        viewModelScope.launch {
            _saveState.value = SaveState.Saving
            log.i { "Starting save process for ${if (_uiState.value.isNewNote) "new" else "existing"} ${currentNote.type} note" }

            try {
                val updatedNote = currentNote.copy(
                    edited = Clock.System.now().toString()
                )

                val result = if (_uiState.value.isNewNote) {
                    notesUseCase.createNote(updatedNote)
                } else {
                    notesUseCase.updateNote(updatedNote)
                }

                result
                    .onSuccess { savedFilePath ->
                        _saveState.value = SaveState.Success(savedFilePath)
                        _uiState.value = _uiState.value.copy(
                            note = updatedNote.copy(filePath = savedFilePath),
                            isNewNote = false
                        )
                        log.i { "Note saved successfully to $savedFilePath" }
                        // No need to manually trigger refresh - the repository handles this automatically
                    }
                    .onFailure { error ->
                        _saveState.value = SaveState.Error(error.message ?: "Failed to save note")
                        log.e { "Error saving note: ${error.message}" }
                    }
            } catch (e: Exception) {
                _saveState.value = SaveState.Error("Unexpected error: ${e.message}")
                log.e(e) { "Unexpected error saving note" }
            }
        }
    }

    fun deleteNote() {
        val currentNote = _uiState.value.note
        if (currentNote.filePath.isNullOrBlank()) {
            log.w { "Cannot delete note without file path" }
            return
        }

        viewModelScope.launch {
            try {
                log.i { "Deleting note: ${currentNote.filePath}" }
                notesUseCase.deleteNote(currentNote.filePath)
                    .onSuccess {
                        log.i { "Note deleted successfully: ${currentNote.filePath}" }
                        // No need to manually trigger refresh - the repository handles this automatically
                    }
                    .onFailure { error ->
                        log.e { "Error deleting note: ${error.message}" }
                        _uiState.value = _uiState.value.copy(
                            error = "Failed to delete note: ${error.message}"
                        )
                    }
            } catch (e: Exception) {
                log.e(e) { "Error deleting note: ${e.message}" }
                _uiState.value = _uiState.value.copy(
                    error = "Failed to delete note: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
        log.d { "Error cleared" }
    }

    fun resetSaveState() {
        _saveState.value = SaveState.Idle
        log.d { "Save state reset to idle" }
    }

    fun hasUnsavedChanges(): Boolean {
        return _saveState.value !is SaveState.Success
    }
}

data class NoteEditUiState(
    val note: EditableNote = EditableNote(),
    val isLoading: Boolean = true,
    val isNewNote: Boolean = true,
    val error: String? = null
)

sealed class SaveState {
    object Idle : SaveState()
    object Saving : SaveState()
    data class Success(val filePath: String) : SaveState()
    data class Error(val message: String) : SaveState()
}