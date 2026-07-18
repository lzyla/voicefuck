package com.aienglishcoach.core.domain.usecase.settings

import com.aienglishcoach.core.domain.model.UserPreferences
import com.aienglishcoach.core.domain.repository.SettingsRepository
import javax.inject.Inject

/** Applies a transformation to the stored preferences. */
class UpdatePreferencesUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
) {
    suspend operator fun invoke(transform: (UserPreferences) -> UserPreferences) =
        settingsRepository.update(transform)
}
