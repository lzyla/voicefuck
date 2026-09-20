package com.aienglishcoach.core.ai

import com.aienglishcoach.core.common.result.AppError
import com.aienglishcoach.core.common.result.AppResult
import com.aienglishcoach.core.domain.model.AiMemory
import com.aienglishcoach.core.domain.model.MemoryKind
import com.aienglishcoach.core.domain.repository.MemoryRepository
import com.aienglishcoach.core.network.OpenAiDataSource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class MemoryManagerTest {

    private val memoryRepository: MemoryRepository = mockk(relaxed = true)
    private val openAiDataSource: OpenAiDataSource = mockk()

    private lateinit var manager: MemoryManager

    private val now = Instant.parse("2026-07-17T10:00:00Z")

    private fun memory(id: Long, content: String, embedding: FloatArray?, importance: Int = 3) =
        AiMemory(
            id = id,
            kind = MemoryKind.FACT,
            content = content,
            embedding = embedding,
            importance = importance,
            createdAt = now,
            updatedAt = now,
        )

    @Before
    fun setUp() {
        manager = MemoryManager(memoryRepository, openAiDataSource)
    }

    @Test
    fun `retrieval ranks by cosine similarity`() = runTest {
        val aboutWork = memory(1, "Works as a nurse", floatArrayOf(1f, 0f))
        val aboutHobby = memory(2, "Loves hiking", floatArrayOf(0f, 1f))
        val aboutFood = memory(3, "Vegetarian", floatArrayOf(0.7f, 0.7f))
        coEvery { memoryRepository.getAll() } returns listOf(aboutWork, aboutHobby, aboutFood)
        // Query embedding points towards the "hobby" axis.
        coEvery { openAiDataSource.embed(any(), any()) } returns
            AppResult.success(listOf(floatArrayOf(0.1f, 0.99f)))

        val top = manager.retrieveRelevant("mountains and trails", topK = 2)

        assertEquals(listOf(2L, 3L), top.map { it.id })
    }

    @Test
    fun `offline retrieval falls back to importance ordering`() = runTest {
        val memories = listOf(
            memory(1, "A", floatArrayOf(1f, 0f), importance = 5),
            memory(2, "B", floatArrayOf(0f, 1f), importance = 4),
            memory(3, "C", null, importance = 3),
        )
        coEvery { memoryRepository.getAll() } returns memories
        coEvery { openAiDataSource.embed(any(), any()) } returns
            AppResult.failure(AppError.Network)

        val top = manager.retrieveRelevant("anything", topK = 2)

        // getAll() order (importance desc) is preserved.
        assertEquals(listOf(1L, 2L), top.map { it.id })
    }

    @Test
    fun `small stores are returned without embedding calls`() = runTest {
        coEvery { memoryRepository.getAll() } returns listOf(memory(1, "only one", null))

        val top = manager.retrieveRelevant("query", topK = 8)

        assertEquals(1, top.size)
        coVerify(exactly = 0) { openAiDataSource.embed(any(), any()) }
    }

    @Test
    fun `backfill stores embeddings for memories without one`() = runTest {
        val withEmbedding = memory(1, "has", floatArrayOf(1f))
        val without = memory(2, "missing", null)
        coEvery { memoryRepository.getAll() } returns listOf(withEmbedding, without)
        coEvery { openAiDataSource.embed(listOf("missing"), any()) } returns
            AppResult.success(listOf(floatArrayOf(0.5f)))

        manager.backfillEmbeddings()

        coVerify { memoryRepository.updateEmbedding(2, any()) }
    }
}
