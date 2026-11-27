package com.gymtracker.backend.service

import com.gymtracker.backend.service.impl.ExerciseImportCommand
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ExerciseImageUrlTest {
    @Test
    fun constructsUrl() {
        assertEquals("https://example.org/main/exercises/Curl/0.jpg",
            ExerciseImportCommand.imageUrl("https://example.org/main/", "Curl", "Curl/0.jpg"))
    }

    @Test
    fun rejectsUnsafePaths() {
        listOf("../0.jpg", "https://evil.org/0.jpg", "Curl/%2f.jpg", "Curl/0.jpg?q=1", "Other/0.jpg").forEach {
            assertFailsWith<IllegalArgumentException> {
                ExerciseImportCommand.imageUrl("https://example.org", "Curl", it)
            }
        }
    }
}
