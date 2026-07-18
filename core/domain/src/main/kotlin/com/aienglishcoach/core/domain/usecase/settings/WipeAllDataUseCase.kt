package com.aienglishcoach.core.domain.usecase.settings

import com.aienglishcoach.core.domain.repository.SettingsRepository
import javax.inject.Inject

/** Irreversibly erases all local user data (GDPR "right to be forgotten"). */
class WipeAllDataUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
) {
    suspend operator fun invoke() = settingsRepository.wipeAllData()
}
