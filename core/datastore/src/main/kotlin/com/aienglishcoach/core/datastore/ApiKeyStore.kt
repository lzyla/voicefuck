package com.aienglishcoach.core.datastore

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.aienglishcoach.core.common.dispatcher.DispatcherProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Encrypted-at-rest storage for the AI API key (Android Keystore-backed).
 *
 * Kept separate from DataStore on purpose: preference files can end up in
 * cloud backups and bug reports, while this file is encrypted and excluded
 * from auto backup (see `backup_rules.xml` in `:app`).
 *
 * NOTE (production): the long-term plan is a backend proxy that holds the
 * provider key server-side; this store then becomes obsolete.
 */
@Singleton
class ApiKeyStore @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dispatcherProvider: DispatcherProvider,
) {

    private val preferences: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    suspend fun set(key: String) = withContext(dispatcherProvider.io) {
        preferences.edit().putString(KEY_API_KEY, key).apply()
    }

    suspend fun get(): String? = withContext(dispatcherProvider.io) {
        preferences.getString(KEY_API_KEY, null)
    }

    suspend fun clear() = withContext(dispatcherProvider.io) {
        preferences.edit().remove(KEY_API_KEY).apply()
    }

    private companion object {
        const val FILE_NAME = "secure_credentials"
        const val KEY_API_KEY = "openai_api_key"
    }
}
