package com.aienglishcoach.core.data.mapper

import com.aienglishcoach.core.domain.model.Conversation
import com.aienglishcoach.core.domain.model.ConversationScenario
import com.aienglishcoach.core.domain.model.ConversationStatus
import com.aienglishcoach.core.domain.model.Exercise
import com.aienglishcoach.core.domain.model.ExerciseType
import com.aienglishcoach.core.domain.model.SrsState
import kotlinx.datetime.Instant
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class MapperTest {

    private val now = Instant.parse("2026-07-17T12:00:00Z")

    @Test
    fun `conversation roundtrips through the entity`() {
        val conversation = Conversation(
            id = 3,
            title = "Travel",
            scenario = ConversationScenario.TRAVEL,
            startedAt = now,
            endedAt = now,
            durationSeconds = 300,
            status = ConversationStatus.ANALYZED,
            summary = "Well done",
        )

        assertEquals(conversation, conversation.toEntity().toDomain())
    }

    @Test
    fun `exercise options survive JSON roundtrip`() {
        val exercise = Exercise(
            id = 1,
            type = ExerciseType.MULTIPLE_CHOICE,
            question = "Choose the right word",
            options = listOf("went", "goed", "gone"),
            correctAnswer = "went",
            explanation = "Past simple",
            sourceErrorId = null,
            srs = SrsState(dueAt = now),
            createdAt = now,
        )

        assertEquals(exercise, exercise.toEntity().toDomain())
    }

    @Test
    fun `unknown enum value in the database degrades to a default`() {
        val entity = Conversation(
            title = "X",
            scenario = ConversationScenario.FREE_TALK,
            startedAt = now,
        ).toEntity().copy(scenario = "REMOVED_SCENARIO", status = "???")

        val domain = entity.toDomain()

        assertEquals(ConversationScenario.FREE_TALK, domain.scenario)
        assertEquals(ConversationStatus.ENDED, domain.status)
    }

    @Test
    fun `embedding roundtrips through little-endian bytes`() {
        val embedding = floatArrayOf(0.1f, -2.5f, 3.14159f, 0f, Float.MIN_VALUE)

        val bytes = embedding.toByteArray()
        val restored = bytes.toFloatArray()

        assertEquals(embedding.size * 4, bytes.size)
        assertArrayEquals(embedding, restored, 0f)
    }
}
