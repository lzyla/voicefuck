package com.aienglishcoach.core.domain.repository

import com.aienglishcoach.core.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow

/** User preferences (DataStore) and the encrypted AI API key. */
interface SettingsRepository {

    val preferences: Flow<UserPreferences>

    suspend fun update(transform: (UserPreferences) -> UserPreferences)

    /** Stores the API key in encrypted storage; never in plain DataStore. */
    suspend fun setApiKey(key: String)

    suspend fun getApiKey(): String?

    suspend fun clearApiKey()

    /** Erases all user data: database, preferences, key, audio files. */
    suspend fun wipeAllData()
}
