package com.eltonkola.everything.data.repository

import com.eltonkola.everything.data.local.EditableNote
import com.eltonkola.everything.data.parser.EvryNote
import com.eltonkola.everything.data.parser.NoteType
import kotlinx.coroutines.flow.Flow

interface NotesRepository {
    // Observables
    val notes: Flow<List<EvryNote>>

    // CRUD Operations
    suspend fun createNote(note: EditableNote): Result<String>
    suspend fun updateNote(note: EditableNote): Result<String>
    suspend fun deleteNote(filePath: String): Result<Unit>
    suspend fun getNote(filePath: String): Result<EditableNote>

    // Query Operations
    suspend fun refreshNotes(): Result<Unit>
    fun getNoteById(id: String): EvryNote?
    fun getNotesByType(type: NoteType): List<EvryNote>
    fun getNotesByTag(tag: String): List<EvryNote>
    fun searchNotes(query: String): List<EvryNote>

    // Cache Management
    fun clearCache()
}
