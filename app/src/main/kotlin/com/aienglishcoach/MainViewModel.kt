package com.aienglishcoach

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aienglishcoach.core.domain.model.ThemeMode
import com.aienglishcoach.core.domain.usecase.settings.ObservePreferencesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** Resolves the app-level state needed before the first frame. */
@HiltViewModel
class MainViewModel @Inject constructor(
    observePreferences: ObservePreferencesUseCase,
) : ViewModel() {

    val uiState: StateFlow<MainUiState> = observePreferences()
        .map { preferences ->
            MainUiState.Ready(
                onboardingCompleted = preferences.onboardingCompleted,
                theme = preferences.theme,
                useDynamicColor = preferences.useDynamicColor,
            ) as MainUiState
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MainUiState.Loading,
        )
}

sealed interface MainUiState {
    data object Loading : MainUiState
    data class Ready(
        val onboardingCompleted: Boolean,
        val theme: ThemeMode,
        val useDynamicColor: Boolean,
    ) : MainUiState
}
