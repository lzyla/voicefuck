package com.aienglishcoach.core.data.repository

import com.aienglishcoach.core.data.mapper.toDomain
import com.aienglishcoach.core.data.mapper.toEntity
import com.aienglishcoach.core.database.dao.ConversationDao
import com.aienglishcoach.core.domain.model.Conversation
import com.aienglishcoach.core.domain.model.ConversationStatus
import com.aienglishcoach.core.domain.model.Message
import com.aienglishcoach.core.domain.repository.ConversationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConversationRepositoryImpl @Inject constructor(
    private val conversationDao: ConversationDao,
) : ConversationRepository {

    override fun observeConversations(): Flow<List<Conversation>> =
        conversationDao.observeAll().map { rows -> rows.map { it.toDomain() } }

    override fun observeConversation(id: Long): Flow<Conversation?> =
        conversationDao.observeById(id).map { it?.toDomain() }

    override fun observeMessages(conversationId: Long): Flow<List<Message>> =
        conversationDao.observeMessages(conversationId).map { rows -> rows.map { it.toDomain() } }

    override suspend fun getConversation(id: Long): Conversation? =
        conversationDao.getById(id)?.toDomain()

    override suspend fun getMessages(conversationId: Long): List<Message> =
        conversationDao.getMessages(conversationId).map { it.toDomain() }

    override suspend fun createConversation(conversation: Conversation): Long =
        conversationDao.insert(conversation.toEntity())

    override suspend fun addMessage(message: Message): Long =
        conversationDao.insertMessage(message.toEntity())

    override suspend fun updateMessageTranslation(messageId: Long, translation: String) =
        conversationDao.updateMessageTranslation(messageId, translation)

    override suspend fun endConversation(id: Long, durationSeconds: Int) =
        conversationDao.markEnded(
            id = id,
            endedAtEpochMillis = Clock.System.now().toEpochMilliseconds(),
            durationSeconds = durationSeconds,
            status = ConversationStatus.ENDED.name,
        )

    override suspend fun updateSummary(id: Long, summary: String, status: ConversationStatus) =
        conversationDao.updateSummary(id, summary, status.name)

    override suspend fun deleteConversation(id: Long) = conversationDao.deleteById(id)
}
