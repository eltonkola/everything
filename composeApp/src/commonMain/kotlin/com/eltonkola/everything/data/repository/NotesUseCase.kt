package com.eltonkola.everything.data.repository

import com.eltonkola.everything.data.local.EditableNote
import com.eltonkola.everything.data.parser.NoteType

class NotesUseCase(
    private val repository: NotesRepository
) {
    val notes = repository.notes

    suspend fun createNote(note: EditableNote) = repository.createNote(note)
    suspend fun updateNote(note: EditableNote) = repository.updateNote(note)
    suspend fun deleteNote(filePath: String) = repository.deleteNote(filePath)
    suspend fun loadNote(filePath: String) = repository.getNote(filePath)
    suspend fun refreshNotes() = repository.refreshNotes()

    fun searchNotes(query: String) = repository.searchNotes(query)
    fun getNotesByType(type: NoteType) = repository.getNotesByType(type)
    fun getNotesByTag(tag: String) = repository.getNotesByTag(tag)
}
