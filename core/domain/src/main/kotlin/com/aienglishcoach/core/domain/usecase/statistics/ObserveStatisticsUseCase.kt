package com.aienglishcoach.core.domain.usecase.statistics

import com.aienglishcoach.core.domain.model.StatisticsOverview
import com.aienglishcoach.core.domain.repository.StatisticsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Streams the aggregated statistics overview for charts and tiles. */
class ObserveStatisticsUseCase @Inject constructor(
    private val statisticsRepository: StatisticsRepository,
) {
    operator fun invoke(): Flow<StatisticsOverview> = statisticsRepository.observeOverview()
}
