package com.aienglishcoach.core.data.repository

import com.aienglishcoach.core.data.mapper.toDomain
import com.aienglishcoach.core.data.mapper.toEntity
import com.aienglishcoach.core.database.dao.UserErrorDao
import com.aienglishcoach.core.domain.model.UserError
import com.aienglishcoach.core.domain.repository.UserErrorRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserErrorRepositoryImpl @Inject constructor(
    private val userErrorDao: UserErrorDao,
) : UserErrorRepository {

    override fun observeAll(): Flow<List<UserError>> =
        userErrorDao.observeAll().map { rows -> rows.map { it.toDomain() } }

    override fun observeForConversation(conversationId: Long): Flow<List<UserError>> =
        userErrorDao.observeForConversation(conversationId)
            .map { rows -> rows.map { it.toDomain() } }

    override suspend fun getUnresolved(limit: Int): List<UserError> =
        userErrorDao.getUnresolved(limit).map { it.toDomain() }

    override suspend fun insertAll(errors: List<UserError>): List<Long> =
        userErrorDao.insertAll(errors.map { it.toEntity() })

    override suspend fun markResolved(id: Long) =
        userErrorDao.markResolved(id, Clock.System.now().toEpochMilliseconds())
}
