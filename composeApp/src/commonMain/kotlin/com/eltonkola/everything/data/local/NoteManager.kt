package com.eltonkola.everything.data.local

import com.eltonkola.everything.data.parser.NoteType
import com.eltonkola.everything.data.repository.FileSystemInterface
import com.eltonkola.everything.di.platform.getNotesDirectory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.time.ExperimentalTime

data class EditableNote(
    val filePath: String = "",
    val type: NoteType = NoteType.TEXT,
    val created: String = "",
    val edited: String = "",
    val tags: List<String> = emptyList(),
    val title: String = "",
    val archived: Boolean = false,
    val content: String = "",
    val todoFields: EditableTodoFields = EditableTodoFields(),
    val locationFields: EditableLocationFields = EditableLocationFields()
)

data class EditableTodoFields(
    val priority: String = "",
    val due: String = "",
    val reminder: Int? = null
)

data class EditableLocationFields(
    val coordinates: String = "",
    val address: String = "",
    val phone: String = "",
    val website: String = "",
    val rating: String = "",
    val cuisine: String = "",
    val priceRange: String = "",
    val hours: String = ""
)

class NoteManager(
    private val fileSystem: FileSystemInterface
) {

    private val notesDirectory: String = getNotesDirectory()

    suspend fun saveNote(note: EditableNote): Result<String> = withContext(Dispatchers.Default) {
        try {
            // Ensure notes directory exists
            createDirectoryIfNotExists(notesDirectory)

            val fileName = generateFileName(note)
            val filePath = "$notesDirectory/$fileName"
            val content = formatNoteContent(note)

            writeFile(filePath, content)



            Result.success(filePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loadNote(filePath: String): Result<EditableNote> = withContext(Dispatchers.Default) {
        try {
            val content = fileSystem.readFile(filePath)
            val parsedNote = parseNoteForEditing(content, filePath)
            Result.success(parsedNote)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteNote(filePath: String): Result<Unit> = withContext(Dispatchers.Default) {
        try {
            fileSystem.delete(filePath)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun createDirectoryIfNotExists(path: String) {
        fileSystem.createDirectory(path)
    }

    private suspend fun writeFile(filePath: String, content: String) {
        fileSystem.writeFile(filePath, content)
    }

    @OptIn(ExperimentalTime::class)
    private fun generateFileName(note: EditableNote): String {
        val timestamp = kotlin.time.Clock.System.now().toString().take(19).replace(":", "-")
        val sanitizedTitle = sanitizeFileName(note.title)

        return if (sanitizedTitle.isNotBlank()) {
            "${timestamp}_${sanitizedTitle}.evry"
        } else {
            "${timestamp}_untitled.evry"
        }
    }

    private fun sanitizeFileName(title: String): String {
        return title
            .take(50) // Limit length
            .replace(Regex("[^a-zA-Z0-9\\s-_]"), "") // Remove special chars
            .replace(Regex("\\s+"), "_") // Replace spaces with underscores
            .trim('_')
    }

    private fun formatNoteContent(note: EditableNote): String {
        val commonSection = buildString {
            appendLine("type=${note.type}")
            appendLine("created=${note.created}")
            if (note.edited.isNotBlank()) appendLine("edited=${note.edited}")
            if (note.tags.isNotEmpty()) appendLine("tags=${note.tags.joinToString(",")}")
            if (note.title.isNotBlank()) appendLine("title=${note.title}")
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
            created = commonFields["created"] ?: kotlin.time.Clock.System.now().toString(),
            edited = commonFields["edited"] ?: "",
            tags = commonFields["tags"]?.split(",")?.map { it.trim() } ?: emptyList(),
            title = commonFields["title"] ?: "",
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
}
