package com.eltonkola.everything.data.repository

import co.touchlab.kermit.Logger
import com.eltonkola.everything.data.local.EditableLocationFields
import com.eltonkola.everything.data.local.EditableNote
import com.eltonkola.everything.data.local.EditableTodoFields
import com.eltonkola.everything.data.parser.EvryNote
import com.eltonkola.everything.data.parser.EvryParser
import com.eltonkola.everything.data.parser.NoteType
import com.eltonkola.everything.di.platform.getNotesDirectory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.onSuccess
import kotlin.time.Clock
import kotlin.time.ExperimentalTime


class EvryRepository(
    private val fileSystem: FileSystemInterface,
    private val parser: EvryParser
) : NotesRepository {

    private val log = Logger.withTag("EvryRepository")
    private val notesDirectory: String = getNotesDirectory()

    // State management
    private val _notes = MutableStateFlow<List<EvryNote>>(emptyList())
    override val notes: Flow<List<EvryNote>> = _notes.asStateFlow()

    // Cache for parsed notes
    private val cache = mutableMapOf<String, CachedNote>()

    // CRUD Operations
    override suspend fun createNote(note: EditableNote): Result<String> {
        return try {
            // Ensure directory exists
            fileSystem.createDirectory(notesDirectory)

            // Generate file path
            val fileName = generateFileName(note)
            val filePath = "$notesDirectory/$fileName"
            val content = formatNoteContent(note)

            // Save file
            fileSystem.writeFile(filePath, content)

            // Update in-memory list
            refreshSingleNote(filePath)

            Result.success(filePath)
        } catch (e: Exception) {
            log.e { "Failed to create note: ${e.message}" }
            Result.failure(e)
        }
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun updateNote(note: EditableNote): Result<String> {
        return try {
            val content = formatNoteContent(note.copy(
                edited = Clock.System.now().toString()
            ))

            // Save file
            fileSystem.writeFile(note.filePath, content)

            // Force refresh this specific note
            cache.remove(note.filePath)
            refreshSingleNote(note.filePath)

            Result.success(note.filePath)
        } catch (e: Exception) {
            log.e { "Failed to update note: ${e.message}" }
            Result.failure(e)
        }
    }

    override suspend fun deleteNote(filePath: String): Result<Unit> {
        return try {
            fileSystem.delete(filePath)
            cache.remove(filePath)

            // Update in-memory list
            val currentNotes = _notes.value.toMutableList()
            currentNotes.removeAll { it.filePath == filePath }
            _notes.value = currentNotes

            Result.success(Unit)
        } catch (e: Exception) {
            log.e { "Failed to delete note: ${e.message}" }
            Result.failure(e)
        }
    }

    override suspend fun getNote(filePath: String): Result<EditableNote> {
        return try {
            val content = fileSystem.readFile(filePath)
            val parsedNote = parseNoteForEditing(content, filePath)
            Result.success(parsedNote)
        } catch (e: Exception) {
            log.e { "Failed to load note: ${e.message}" }
            Result.failure(e)
        }
    }

    override suspend fun refreshNotes(): Result<Unit> {
        return scanDirectory(notesDirectory)
    }

    override fun getNoteById(id: String): EvryNote? {
        return _notes.value.find { it.id == id }
    }

    override fun getNotesByType(type: NoteType): List<EvryNote> {
        return _notes.value.filter { it.commonFields.type == type }
    }

    override fun getNotesByTag(tag: String): List<EvryNote> {
        return _notes.value.filter { note ->
            note.commonFields.tags.any { it.equals(tag, ignoreCase = true) }
        }
    }

    override fun searchNotes(query: String): List<EvryNote> {
        return _notes.value.filter { note ->
            note.commonFields.title?.contains(query, ignoreCase = true) == true ||
                    note.content.contains(query, ignoreCase = true) ||
                    note.commonFields.tags.any { it.contains(query, ignoreCase = true) }
        }
    }

    override fun clearCache() {
        cache.clear()
    }

    // Internal scanning logic (from EvryRepository)
    private suspend fun scanDirectory(rootPath: String): Result<Unit> {
        log.i { "Scanning directory $rootPath" }
        return try {
            val allFiles = scanDirectoryRecursive(rootPath)
            val evryFiles = allFiles.filter { it.endsWith(".evry") }
            val newNotes = mutableListOf<EvryNote>()

            evryFiles.forEach { filePath ->
                val result = loadAndParseFile(filePath)
                result.getOrNull()?.let { note ->
                    newNotes.add(note)
                }
            }

            _notes.value = newNotes
            log.i { "Loaded ${newNotes.size} notes" }
            Result.success(Unit)
        } catch (e: Exception) {
            log.e { "Error scanning directory: ${e.message}" }
            Result.failure(e)
        }
    }

    private suspend fun scanDirectoryRecursive(directoryPath: String): List<String> {
        val allFiles = mutableListOf<String>()
        val directories = mutableListOf(directoryPath)

        while (directories.isNotEmpty()) {
            val currentDir = directories.removeFirst()
            try {
                val files = fileSystem.listFiles(currentDir)
                files.forEach { file ->
                    if (fileSystem.isDirectory(file)) {
                        directories.add(file)
                    } else {
                        allFiles.add(file)
                    }
                }
            } catch (e: Exception) {
                log.e { "Error scanning directory $currentDir: ${e.message}" }
                continue
            }
        }
        return allFiles
    }

    private suspend fun loadAndParseFile(filePath: String): Result<EvryNote> {
        return try {
            val lastModified = fileSystem.getLastModified(filePath)
            val cachedNote = cache[filePath]

            // Check cache
            if (cachedNote != null && cachedNote.lastModified >= lastModified) {
                return Result.success(cachedNote.note)
            }

            // Parse file
            val content = fileSystem.readFile(filePath)
            val parseResult = parser.parseFile(content, filePath)

            parseResult.onSuccess { note ->
                cache[filePath] = CachedNote(note, lastModified)
            }

            parseResult
        } catch (e: Exception) {
            log.e { "Failed parsing file $filePath: ${e.message}" }
            Result.failure(e)
        }
    }

    private suspend fun refreshSingleNote(filePath: String) {
        try {
            cache.remove(filePath)
            val result = loadAndParseFile(filePath)

            result.onSuccess { newNote ->
                val currentNotes = _notes.value.toMutableList()
                val index = currentNotes.indexOfFirst { it.filePath == filePath }
                if (index >= 0) {
                    currentNotes[index] = newNote
                } else {
                    currentNotes.add(newNote)
                }
                _notes.value = currentNotes
            }
        } catch (e: Exception) {
            log.e { "Failed to refresh single note: ${e.message}" }
        }
    }

    // File formatting logic (from NoteManager)
    @OptIn(ExperimentalTime::class)
    private fun generateFileName(note: EditableNote): String {
        val timestamp = Clock.System.now().toString().take(19).replace(":", "-")
        val sanitizedTitle = sanitizeFileName(note.title)

        return if (sanitizedTitle.isNotBlank()) {
            "${timestamp}_${sanitizedTitle}.evry"
        } else {
            "${timestamp}_untitled.evry"
        }
    }

    private fun sanitizeFileName(title: String): String {
        return title
            .take(50)
            .replace(Regex("[^a-zA-Z0-9\\s-_]"), "")
            .replace(Regex("\\s+"), "_")
            .trim('_')
    }

    private fun formatNoteContent(note: EditableNote): String {
        val commonSection = buildString {
            appendLine("type=${note.type.value}")
            appendLine("created=${note.created}")
            if (note.edited.isNotBlank()) appendLine("edited=${note.edited}")
            if (note.tags.isNotEmpty()) appendLine("tags=${note.tags.joinToString(",")}")
            if (note.title.isNotBlank()) appendLine("title=${note.title}")
            if (note.archived) appendLine("archived=true")
        }

        val typeSection = when (note.type) {
            NoteType.TODO -> formatTodoFields(note.todoFields)
            NoteType.LOCATION -> formatLocationFields(note.locationFields)
            else -> ""
        }

        return buildString {
            append(commonSection.trim())
            appendLine()
            appendLine("------")
            if (typeSection.isNotBlank()) {
                append(typeSection.trim())
                appendLine()
            }
            appendLine("------")
            append(note.content)
        }
    }

    private fun formatTodoFields(fields: EditableTodoFields): String {
        return buildString {
            if (fields.priority.isNotBlank()) appendLine("priority[${fields.priority}]")
            if (fields.due.isNotBlank()) appendLine("due[${fields.due}]")
            if (fields.reminder != null) appendLine("reminder[${fields.reminder}]")
        }
    }

    private fun formatLocationFields(fields: EditableLocationFields): String {
        return buildString {
            if (fields.coordinates.isNotBlank()) appendLine("coordinates[${fields.coordinates}]")
            if (fields.address.isNotBlank()) appendLine("address[${fields.address}]")
            if (fields.phone.isNotBlank()) appendLine("phone[${fields.phone}]")
            if (fields.website.isNotBlank()) appendLine("website[${fields.website}]")
            if (fields.rating.isNotBlank()) appendLine("rating[${fields.rating}]")
            if (fields.cuisine.isNotBlank()) appendLine("cuisine[${fields.cuisine}]")
            if (fields.priceRange.isNotBlank()) appendLine("price_range[${fields.priceRange}]")
            if (fields.hours.isNotBlank()) appendLine("hours[${fields.hours}]")
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun parseNoteForEditing(content: String, filePath: String): EditableNote {
        val sections = content.split("------")
        val commonFields = parseCommonFieldsForEditing(sections[0])
        val typeFields = if (sections.size > 1) sections[1].trim() else ""
        val noteContent = if (sections.size > 2) sections[2].trim() else ""

        return EditableNote(
            filePath = filePath,
            type = NoteType.fromString(commonFields["type"] ?: "note"),
            created = commonFields["created"] ?: Clock.System.now().toString(),
            edited = commonFields["edited"] ?: "",
            tags = commonFields["tags"]?.split(",")?.map { it.trim() } ?: emptyList(),
            title = commonFields["title"] ?: "",
            archived = commonFields["archived"]?.toBoolean() ?: false,
            content = noteContent,
            todoFields = if (commonFields["type"] == "todo") parseTodoFieldsForEditing(typeFields) else EditableTodoFields(),
            locationFields = if (commonFields["type"] == "location") parseLocationFieldsForEditing(typeFields) else EditableLocationFields()
        )
    }

    private fun parseCommonFieldsForEditing(section: String): Map<String, String> {
        val fieldRegex = Regex("""(\w+)(?:\[([^\]]*)\]|=(.*))?""")
        return section.lines()
            .mapNotNull { line ->
                fieldRegex.find(line.trim())?.let { match ->
                    val key = match.groupValues[1]
                    val bracketValue = match.groupValues[2]
                    val equalValue = match.groupValues[3]
                    val value = bracketValue.ifEmpty { equalValue }
                    key to value
                }
            }
            .toMap()
    }

    private fun parseTodoFieldsForEditing(section: String): EditableTodoFields {
        val fields = parseCommonFieldsForEditing(section)
        return EditableTodoFields(
            priority = fields["priority"] ?: "",
            due = fields["due"] ?: "",
            reminder = fields["reminder"]?.toIntOrNull()
        )
    }

    private fun parseLocationFieldsForEditing(section: String): EditableLocationFields {
        val fields = parseCommonFieldsForEditing(section)
        return EditableLocationFields(
            coordinates = fields["coordinates"] ?: "",
            address = fields["address"] ?: "",
            phone = fields["phone"] ?: "",
            website = fields["website"] ?: "",
            rating = fields["rating"] ?: "",
            cuisine = fields["cuisine"] ?: "",
            priceRange = fields["price_range"] ?: "",
            hours = fields["hours"] ?: ""
        )
    }

    private data class CachedNote(
        val note: EvryNote,
        val lastModified: Long
    )

}