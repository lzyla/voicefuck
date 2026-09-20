package com.aienglishcoach.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aienglishcoach.core.domain.model.EnglishLevel
import com.aienglishcoach.core.domain.usecase.home.ObserveHomeSummaryUseCase
import com.aienglishcoach.core.domain.usecase.settings.ObservePreferencesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    observePreferences: ObservePreferencesUseCase,
    observeHomeSummary: ObserveHomeSummaryUseCase,
) : ViewModel() {

    val uiState: StateFlow<ProfileUiState> = combine(
        observePreferences(),
        observeHomeSummary(),
    ) { preferences, summary ->
        ProfileUiState(
            englishLevel = preferences.englishLevel,
            streakDays = summary.streakDays,
            achievements = ACHIEVEMENTS.map { achievement ->
                achievement.copy(unlocked = summary.streakDays >= achievement.streakRequirement)
            },
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = ProfileUiState(),
    )
}

data class Achievement(
    val titleRes: Int,
    val streakRequirement: Int,
    val unlocked: Boolean = false,
)

data class ProfileUiState(
    val englishLevel: EnglishLevel = EnglishLevel.B1,
    val streakDays: Int = 0,
    val achievements: List<Achievement> = emptyList(),
)

private val ACHIEVEMENTS = listOf(
    Achievement(titleRes = R.string.profile_achievement_first_step, streakRequirement = 1),
    Achievement(titleRes = R.string.profile_achievement_three_days, streakRequirement = 3),
    Achievement(titleRes = R.string.profile_achievement_one_week, streakRequirement = 7),
    Achievement(titleRes = R.string.profile_achievement_one_month, streakRequirement = 30),
)
