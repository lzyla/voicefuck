package com.aienglishcoach.feature.statistics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Quiz
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Style
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aienglishcoach.core.designsystem.component.CoachCard
import com.aienglishcoach.core.designsystem.component.EmptyState
import com.aienglishcoach.core.designsystem.component.LoadingIndicator
import com.aienglishcoach.core.designsystem.component.StatTile
import com.aienglishcoach.core.designsystem.theme.Spacing
import com.aienglishcoach.core.domain.model.DailyStats
import com.aienglishcoach.core.domain.model.StatisticsOverview

/**
 * Statistics tab: tiles with the key learning metrics and a 30-day
 * conversation-activity bar chart drawn with a plain Compose [Canvas].
 */
@Composable
fun StatisticsScreen(viewModel: StatisticsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.statistics_title)) })
        },
    ) { padding ->
        when (val state = uiState) {
            StatisticsUiState.Loading -> LoadingIndicator(modifier = Modifier.padding(padding))
            is StatisticsUiState.Ready -> StatisticsContent(
                overview = state.overview,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            )
        }
    }
}

@Composable
private fun StatisticsContent(
    overview: StatisticsOverview,
    modifier: Modifier = Modifier,
) {
    if (!overview.hasAnyActivity()) {
        EmptyState(
            icon = Icons.Rounded.Insights,
            title = stringResource(R.string.statistics_empty_title),
            description = stringResource(R.string.statistics_empty_description),
            modifier = modifier,
        )
        return
    }
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        StatTileGrid(overview = overview)
        ActivityChartCard(days = overview.recentDays)
    }
}

@Composable
private fun StatTileGrid(overview: StatisticsOverview) {
    val tiles = buildList {
        add(
            TileData(
                value = overview.streakDays.toString(),
                label = stringResource(R.string.statistics_streak),
                icon = Icons.Rounded.LocalFireDepartment,
            ),
        )
        add(
            TileData(
                value = overview.totalConversationMinutes.toString(),
                label = stringResource(R.string.statistics_total_minutes),
                icon = Icons.Rounded.Schedule,
            ),
        )
        add(
            TileData(
                value = overview.totalWordsLearned.toString(),
                label = stringResource(R.string.statistics_words_learned),
                icon = Icons.Rounded.Style,
            ),
        )
        add(
            TileData(
                value = overview.totalExercisesDone.toString(),
                label = stringResource(R.string.statistics_exercises_done),
                icon = Icons.Rounded.Quiz,
            ),
        )
        add(
            TileData(
                value = stringResource(
                    R.string.statistics_percent_value,
                    overview.exerciseAccuracyPercent,
                ),
                label = stringResource(R.string.statistics_accuracy),
                icon = Icons.Rounded.CheckCircle,
            ),
        )
        overview.averagePronunciationScore?.let { score ->
            add(
                TileData(
                    value = stringResource(R.string.statistics_percent_value, score),
                    label = stringResource(R.string.statistics_pronunciation),
                    icon = Icons.Rounded.RecordVoiceOver,
                ),
            )
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
        tiles.chunked(2).forEach { rowTiles ->
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                rowTiles.forEach { tile ->
                    StatTile(
                        value = tile.value,
                        label = tile.label,
                        icon = tile.icon,
                        modifier = Modifier.weight(1f),
                    )
                }
                if (rowTiles.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

/** Bar chart of conversation minutes per day for the last 30 days. */
@Composable
private fun ActivityChartCard(days: List<DailyStats>) {
    CoachCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth().padding(Spacing.md)) {
            Text(
                text = stringResource(R.string.statistics_chart_title),
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.height(Spacing.md))
            val barColor = MaterialTheme.colorScheme.primary
            val baselineColor = MaterialTheme.colorScheme.outlineVariant
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ChartHeight),
            ) {
                if (days.isEmpty()) return@Canvas
                val maxMinutes = days
                    .maxOf { it.conversationSeconds }
                    .coerceAtLeast(60) / 60f
                val slotWidth = size.width / days.size
                val barWidth = slotWidth * BarWidthFraction
                val baselineHeight = 2.dp.toPx()
                days.forEachIndexed { index, day ->
                    val minutes = day.conversationSeconds / 60f
                    val left = index * slotWidth + (slotWidth - barWidth) / 2f
                    if (minutes > 0f) {
                        val barHeight = (minutes / maxMinutes * size.height)
                            .coerceAtLeast(baselineHeight)
                        drawRect(
                            color = barColor,
                            topLeft = Offset(left, size.height - barHeight),
                            size = Size(barWidth, barHeight),
                        )
                    } else {
                        drawRect(
                            color = baselineColor,
                            topLeft = Offset(left, size.height - baselineHeight),
                            size = Size(barWidth, baselineHeight),
                        )
                    }
                }
            }
        }
    }
}

private fun StatisticsOverview.hasAnyActivity(): Boolean =
    streakDays > 0 ||
        totalConversationMinutes > 0 ||
        totalWordsLearned > 0 ||
        totalExercisesDone > 0 ||
        recentDays.any { it.conversationSeconds > 0 }

/** One metric tile of the statistics grid. */
private data class TileData(
    val value: String,
    val label: String,
    val icon: ImageVector,
)

private val ChartHeight = 160.dp
private const val BarWidthFraction = 0.6f
