package com.aienglishcoach.core.domain.usecase.settings

import com.aienglishcoach.core.domain.repository.SettingsRepository
import javax.inject.Inject

/**
 * Stores the AI API key after basic validation. The key lands in encrypted
 * storage only — it must never be written to DataStore, logs or backups.
 */
class SetApiKeyUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
) {

    suspend operator fun invoke(rawKey: String): Boolean {
        val key = rawKey.trim()
        if (key.length < MIN_KEY_LENGTH) return false
        settingsRepository.setApiKey(key)
        return true
    }

    companion object {
        const val MIN_KEY_LENGTH = 20
    }
}
