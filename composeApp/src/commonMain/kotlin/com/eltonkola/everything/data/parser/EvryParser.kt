package com.eltonkola.everything.data.parser


class EvryParser() {

    fun parseFile(content: String, filePath: String): Result<EvryNote> {
        return try {
            val sections = splitIntoSections(content)
            val commonFields = parseCommonFields(sections.commonSection)
            val typeFields = parseTypeFields(sections.typeSection)
            val noteContent = sections.contentSection

            val note = when (commonFields.type) {
                NoteType.TODO -> createTodoNote(commonFields, typeFields, noteContent, filePath)
                NoteType.LOCATION -> createLocationNote(commonFields, typeFields, noteContent, filePath)
                else -> createTextNote(commonFields, noteContent, filePath)
            }

            Result.success(note)
        } catch (e: Exception) {
            Result.failure(ParseException("Failed to parse file: ${e.message}", e))
        }
    }

    private fun splitIntoSections(content: String): NoteSections {
        val parts = content.split("------")

        return when (parts.size) {
            1 -> NoteSections(parts[0], "", "")
            2 -> NoteSections(parts[0], "", parts[1])
            3 -> NoteSections(parts[0], parts[1], parts[2])
            else -> NoteSections(
                parts[0],
                parts[1],
                parts.drop(2).joinToString("------")
            )
        }
    }

    private fun parseCommonFields(section: String): CommonFields {
        val fields = parseFieldSection(section)

        return CommonFields(
            type =  NoteType.fromString(fields["type"] ?: throw ParseException("Missing required field: type") ),
            created = fields["created"] ?: throw ParseException("Missing required field: created"),
            edited = fields["edited"],
            tags = fields["tags"]?.split(",")?.map { it.trim() } ?: emptyList(),
            title = fields["title"],
            archived = fields["archived"]?.toBoolean() ?: false
        )
    }

    private fun parseTypeFields(section: String): Map<String, String> {
        return parseFieldSection(section)
    }

    private fun parseFieldSection(section: String): Map<String, String> {
        val fieldRegex = Regex("""(\w+)(?:\[([^\]]*)\]|=(.*))?""")

        return section.lines()
            .mapNotNull { line ->
                val trimmed = line.trim()
                if (trimmed.isEmpty()) return@mapNotNull null

                fieldRegex.find(trimmed)?.let { match ->
                    val key = match.groupValues[1]
                    val bracketValue = match.groupValues[2]
                    val equalValue = match.groupValues[3]
                    val value = bracketValue.ifEmpty { equalValue }
                    key to value
                }
            }
            .toMap()
    }

    private fun createTextNote(
        commonFields: CommonFields,
        content: String,
        filePath: String
    ): EvryNote.TextNote {
        return EvryNote.TextNote(
            id = generateId(filePath),
            commonFields = commonFields,
            content = content.trim(),
            filePath = filePath
        )
    }

    private fun createTodoNote(
        commonFields: CommonFields,
        typeFields: Map<String, String>,
        content: String,
        filePath: String
    ): EvryNote.TodoNote {
        val todoFields = TodoFields(
            priority = typeFields["priority"],
            due = typeFields["due"],
            reminder = typeFields["reminder"]?.toIntOrNull()
        )

        return EvryNote.TodoNote(
            id = generateId(filePath),
            commonFields = commonFields,
            content = content.trim(),
            filePath = filePath,
            todoFields = todoFields
        )
    }

    private fun createLocationNote(
        commonFields: CommonFields,
        typeFields: Map<String, String>,
        content: String,
        filePath: String
    ): EvryNote.LocationNote {
        val coordinates = parseCoordinates(typeFields["coordinates"])

        val locationFields = LocationFields(
            coordinates = coordinates,
            address = typeFields["address"],
            phone = typeFields["phone"],
            website = typeFields["website"],
            rating = typeFields["rating"],
            cuisine = typeFields["cuisine"],
            priceRange = typeFields["price_range"],
            hours = typeFields["hours"]
        )

        return EvryNote.LocationNote(
            id = generateId(filePath),
            commonFields = commonFields,
            content = content.trim(),
            filePath = filePath,
            locationFields = locationFields
        )
    }

    private fun parseCoordinates(coordsString: String?): Pair<Double, Double>? {
        if (coordsString.isNullOrBlank()) return null

        val coords = coordsString.split(",").map { it.trim() }
        return if (coords.size == 2) {
            try {
                Pair(coords[0].toDouble(), coords[1].toDouble())
            } catch (e: NumberFormatException) {
                null
            }
        } else null
    }

    private fun generateId(filePath: String): String {
        return filePath.hashCode().toString()
    }

    private data class NoteSections(
        val commonSection: String,
        val typeSection: String,
        val contentSection: String
    )
}

class ParseException(message: String, cause: Throwable? = null) : Exception(message, cause)
