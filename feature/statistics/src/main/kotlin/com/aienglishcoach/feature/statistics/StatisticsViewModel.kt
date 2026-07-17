package com.aienglishcoach.feature.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aienglishcoach.core.domain.model.StatisticsOverview
import com.aienglishcoach.core.domain.usecase.statistics.ObserveStatisticsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** Streams the aggregated learning statistics for the statistics tab. */
@HiltViewModel
class StatisticsViewModel @Inject constructor(
    observeStatistics: ObserveStatisticsUseCase,
) : ViewModel() {

    val uiState: StateFlow<StatisticsUiState> = observeStatistics()
        .map<StatisticsOverview, StatisticsUiState> { StatisticsUiState.Ready(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = StatisticsUiState.Loading,
        )

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}

/** UI state of the statistics screen. */
sealed interface StatisticsUiState {
    data object Loading : StatisticsUiState
    data class Ready(val overview: StatisticsOverview) : StatisticsUiState
}
