package com.aienglishcoach.core.domain.repository

import com.aienglishcoach.core.domain.model.Conversation
import com.aienglishcoach.core.domain.model.ConversationStatus
import com.aienglishcoach.core.domain.model.Message
import kotlinx.coroutines.flow.Flow

/** Local-first access to conversations and their messages. */
interface ConversationRepository {

    fun observeConversations(): Flow<List<Conversation>>

    fun observeConversation(id: Long): Flow<Conversation?>

    fun observeMessages(conversationId: Long): Flow<List<Message>>

    suspend fun getConversation(id: Long): Conversation?

    suspend fun getMessages(conversationId: Long): List<Message>

    /** Creates a conversation and returns its id. */
    suspend fun createConversation(conversation: Conversation): Long

    /** Appends a message and returns its id. */
    suspend fun addMessage(message: Message): Long

    suspend fun updateMessageTranslation(messageId: Long, translation: String)

    suspend fun endConversation(id: Long, durationSeconds: Int)

    suspend fun updateSummary(id: Long, summary: String, status: ConversationStatus)

    suspend fun deleteConversation(id: Long)
}
