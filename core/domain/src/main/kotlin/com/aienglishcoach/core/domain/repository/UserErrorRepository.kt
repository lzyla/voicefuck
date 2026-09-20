package com.aienglishcoach.core.domain.repository

import com.aienglishcoach.core.domain.model.UserError
import kotlinx.coroutines.flow.Flow

/** Storage for language mistakes detected in conversations. */
interface UserErrorRepository {

    fun observeAll(): Flow<List<UserError>>

    fun observeForConversation(conversationId: Long): Flow<List<UserError>>

    suspend fun getUnresolved(limit: Int): List<UserError>

    suspend fun insertAll(errors: List<UserError>): List<Long>

    suspend fun markResolved(id: Long)
}
