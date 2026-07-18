package com.aienglishcoach.core.domain.usecase.settings

import com.aienglishcoach.core.domain.model.UserPreferences
import com.aienglishcoach.core.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Streams the user's preferences. */
class ObservePreferencesUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
) {
    operator fun invoke(): Flow<UserPreferences> = settingsRepository.preferences
}
