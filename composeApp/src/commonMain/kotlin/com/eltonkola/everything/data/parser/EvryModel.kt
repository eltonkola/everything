package com.eltonkola.everything.data.parser

import kotlinx.serialization.Serializable

@Serializable
sealed class EvryNote {
    abstract val id: String
    abstract val commonFields: CommonFields
    abstract val content: String
    abstract val filePath: String

    @Serializable
    data class TextNote(
        override val id: String,
        override val commonFields: CommonFields,
        override val content: String,
        override val filePath: String,
    ) : EvryNote()

    @Serializable
    data class TodoNote(
        override val id: String,
        override val commonFields: CommonFields,
        override val content: String,
        override val filePath: String,
        val todoFields: TodoFields
    ) : EvryNote()

    @Serializable
    data class LocationNote(
        override val id: String,
        override val commonFields: CommonFields,
        override val content: String,
        override val filePath: String,
        val locationFields: LocationFields
    ) : EvryNote()
}

@Serializable
data class CommonFields(
    val type: NoteType,
    val created: String,
    val edited: String? = null,
    val tags: List<String> = emptyList(),
    val title: String? = null,
    val archived: Boolean = false
)

@Serializable
enum class NoteType(val value: String, val title: String) {
    TEXT("note", "Text"),
    TODO("todo", "List"),
    LOCATION("location", "Map");

    companion object {
        fun fromString(value: String): NoteType {
            return values().find { it.value.equals(value, ignoreCase = true) } ?: TEXT
        }
    }
}

@Serializable
data class TodoFields(
    val priority: String? = null,
    val due: String? = null,
    val reminder: Int? = null
)

@Serializable
data class LocationFields(
    val coordinates: Pair<Double, Double>? = null,
    val address: String? = null,
    val phone: String? = null,
    val website: String? = null,
    val rating: String? = null,
    val cuisine: String? = null,
    val priceRange: String? = null,
    val hours: String? = null
)