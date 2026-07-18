package com.aienglishcoach.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aienglishcoach.core.domain.usecase.home.HomeSummary
import com.aienglishcoach.core.domain.usecase.home.ObserveHomeSummaryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    observeHomeSummary: ObserveHomeSummaryUseCase,
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = observeHomeSummary()
        .map<HomeSummary, HomeUiState> { HomeUiState.Ready(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = HomeUiState.Loading,
        )

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Ready(val summary: HomeSummary) : HomeUiState
}
