package com.eltonkola.everything.data.parser

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class EvryParserTest {

    private val parser = EvryParser()
    private val testFilePath = "/test/path/note.evry"

    @Test
    fun `parseFile should parse basic text note correctly`() {
        val content = """type=note
created=2023-01-01T10:00:00
title=My Test Note
tags=important,work

This is the content of my note.
It can span multiple lines.
------
This is the end."""

        val result = parser.parseFile(content, testFilePath)

        assertTrue(result.isSuccess)
        val note = result.getOrNull()
        assertNotNull(note)
        assertIs<EvryNote.TextNote>(note)
        assertEquals("note", note.commonFields.type)
        assertEquals("2023-01-01T10:00:00", note.commonFields.created)
        assertEquals("My Test Note", note.commonFields.title)
        assertEquals(listOf("important", "work"), note.commonFields.tags)
        assertEquals("This is the content of my note.\nIt can span multiple lines.\n------\nThis is the end.", note.content)
        assertEquals(testFilePath, note.filePath)
    }

    @Test
    fun `parseFile should parse todo note correctly`() {
        val content = """type=todo
created=2023-01-01T10:00:00
edited=2023-01-02T11:00:00
title=Buy groceries
tags=shopping,urgent
priority[high]
due[2023-01-03]
reminder[60]

Milk
Bread
Eggs"""

        val result = parser.parseFile(content, testFilePath)

        assertTrue(result.isSuccess)
        val note = result.getOrNull()
        assertNotNull(note)
        assertIs<EvryNote.TodoNote>(note)
        assertEquals("todo", note.commonFields.type)
        assertEquals("Buy groceries", note.commonFields.title)
        assertEquals(listOf("shopping", "urgent"), note.commonFields.tags)
        assertEquals("2023-01-01T10:00:00", note.commonFields.created)
        assertEquals("2023-01-02T11:00:00", note.commonFields.edited)

        val todoFields = note.todoFields
        assertEquals("high", todoFields.priority)
        assertEquals("2023-01-03", todoFields.due)
        assertEquals(60, todoFields.reminder)
    }

    @Test
    fun `parseFile should parse location note correctly`() {
        val content = """type=location
created=2023-01-01T10:00:00
title=Favorite Restaurant
tags=food,dining
coordinates[40.7128,-74.0060]
address[123 Main St, New York, NY 10001]
phone[(555) 123-4567]
website[https://restaurant.com]
rating[4.5]
cuisine[Italian]
price_range[$$]
hours[Mon-Fri: 11am-10pm]

Great pasta and service!"""

        val result = parser.parseFile(content, testFilePath)

        assertTrue(result.isSuccess)
        val note = result.getOrNull()
        assertNotNull(note)
        assertIs<EvryNote.LocationNote>(note)
        assertEquals("location", note.commonFields.type)
        assertEquals("Favorite Restaurant", note.commonFields.title)
        assertEquals(listOf("food", "dining"), note.commonFields.tags)

        val locationFields = note.locationFields
        assertNotNull(locationFields.coordinates)
        assertEquals(Pair(40.7128, -74.0060), locationFields.coordinates)
        assertEquals("123 Main St, New York, NY 10001", locationFields.address)
        assertEquals("(555) 123-4567", locationFields.phone)
        assertEquals("https://restaurant.com", locationFields.website)
        assertEquals("4.5", locationFields.rating)
        assertEquals("Italian", locationFields.cuisine)
        assertEquals("$$", locationFields.priceRange)
        assertEquals("Mon-Fri: 11am-10pm", locationFields.hours)
    }

    @Test
    fun `parseFile should handle minimal text note`() {
        val content = """type=note
created=2023-01-01T10:00:00

Simple note"""

        val result = parser.parseFile(content, testFilePath)

        assertTrue(result.isSuccess)
        val note = result.getOrNull()
        assertNotNull(note)
        assertIs<EvryNote.TextNote>(note)
        assertEquals("note", note.commonFields.type)
        assertEquals("2023-01-01T10:00:00", note.commonFields.created)
        assertEquals("", note.commonFields.title)
        assertEquals(emptyList(), note.commonFields.tags)
        assertEquals("Simple note", note.content)
    }

    @Test
    fun `parseFile should fail when type is missing`() {
        val content = """created=2023-01-01T10:00:00
title=Test Note

Content"""

        val result = parser.parseFile(content, testFilePath)

        assertTrue(result.isFailure)
        assertIs<ParseException>(result.exceptionOrNull())
    }

    @Test
    fun `parseFile should fail when created is missing`() {
        val content = """type=note
title=Test Note

Content"""

        val result = parser.parseFile(content, testFilePath)

        assertTrue(result.isFailure)
        assertIs<ParseException>(result.exceptionOrNull())
    }

    @Test
    fun `parseFile should handle invalid coordinates gracefully`() {
        val content = """type=location
created=2023-01-01T10:00:00
coordinates[invalid]
address[123 Main St]

Location content"""

        val result = parser.parseFile(content, testFilePath)

        assertTrue(result.isSuccess)
        val note = result.getOrNull()
        assertNotNull(note)
        assertIs<EvryNote.LocationNote>(note)
        assertNull(note.locationFields.coordinates)
    }

    @Test
    fun `parseFile should handle malformed field syntax`() {
        val content = """type=note
created=2023-01-01T10:00:00
invalid_field_format
title=Test

Content"""

        val result = parser.parseFile(content, testFilePath)

        // Should still parse successfully, ignoring malformed fields
        assertTrue(result.isSuccess)
        val note = result.getOrNull()
        assertNotNull(note)
        assertIs<EvryNote.TextNote>(note)
        assertEquals("Test", note.commonFields.title)
    }

    @Test
    fun `parseFile should handle different section counts`() {
        // Single section (no separators)
        val content1 = """type=note
created=2023-01-01T10:00:00

Content"""

        val result1 = parser.parseFile(content1, testFilePath)
        assertTrue(result1.isSuccess)

        // Two sections
        val content2 = """type=note
created=2023-01-01T10:00:00
------
Content"""

        val result2 = parser.parseFile(content2, testFilePath)
        assertTrue(result2.isSuccess)

        // More than three sections
        val content3 = """type=note
created=2023-01-01T10:00:00
------
type_section
------
content
------
extra"""

        val result3 = parser.parseFile(content3, testFilePath)
        assertTrue(result3.isSuccess)
        val note = result3.getOrNull()
        assertNotNull(note)
        assertEquals("content\n------\nextra", note.content)
    }
}
