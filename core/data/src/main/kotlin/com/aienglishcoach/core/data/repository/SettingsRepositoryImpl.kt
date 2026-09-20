package com.aienglishcoach.core.data.repository

import android.content.Context
import com.aienglishcoach.core.common.dispatcher.DispatcherProvider
import com.aienglishcoach.core.database.CoachDatabase
import com.aienglishcoach.core.datastore.ApiKeyStore
import com.aienglishcoach.core.datastore.UserPreferencesDataSource
import com.aienglishcoach.core.domain.model.UserPreferences
import com.aienglishcoach.core.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userPreferencesDataSource: UserPreferencesDataSource,
    private val apiKeyStore: ApiKeyStore,
    private val database: CoachDatabase,
    private val dispatcherProvider: DispatcherProvider,
) : SettingsRepository {

    override val preferences: Flow<UserPreferences> = userPreferencesDataSource.preferences

    override suspend fun update(transform: (UserPreferences) -> UserPreferences) =
        userPreferencesDataSource.update(transform)

    override suspend fun setApiKey(key: String) = apiKeyStore.set(key)

    override suspend fun getApiKey(): String? = apiKeyStore.get()

    override suspend fun clearApiKey() = apiKeyStore.clear()

    override suspend fun wipeAllData() {
        withContext(dispatcherProvider.io) {
            database.clearAllTables()
            File(context.filesDir, AUDIO_DIRECTORY).deleteRecursively()
        }
        apiKeyStore.clear()
        userPreferencesDataSource.clear()
        Timber.i("All user data wiped")
    }

    companion object {
        /** Must match the directory used by `:core:audio` AudioRecorder. */
        const val AUDIO_DIRECTORY = "audio"
    }
}
