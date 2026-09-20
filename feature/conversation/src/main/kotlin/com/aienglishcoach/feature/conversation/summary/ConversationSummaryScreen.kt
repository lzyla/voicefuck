package com.aienglishcoach.feature.conversation.summary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aienglishcoach.core.designsystem.glass.AuroraBackground
import com.aienglishcoach.core.designsystem.glass.GlassPanel
import com.aienglishcoach.core.designsystem.glass.GlassPrimaryButton
import com.aienglishcoach.core.designsystem.glass.GlassTokens
import com.aienglishcoach.core.domain.model.ErrorCategory
import com.aienglishcoach.core.domain.model.UserError
import com.aienglishcoach.feature.conversation.R
import com.aienglishcoach.feature.conversation.errorMessage

/** Analysis results: summary, detected errors with corrections, next steps. */
@Composable
fun ConversationSummaryScreen(
    onBack: () -> Unit,
    onGoToExercises: () -> Unit,
    viewModel: ConversationSummaryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        AuroraBackground()
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.summary_title), color = GlassTokens.TextPrimary) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = stringResource(R.string.conversation_back),
                                tint = GlassTokens.TextPrimary,
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                )
            },
        ) { padding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(GlassTokens.ScreenSidePadding),
                verticalArrangement = Arrangement.spacedBy(GlassTokens.CardGap),
            ) {
                if (uiState.isAnalyzing) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            CircularProgressIndicator(color = GlassTokens.Accent)
                            Text(
                                text = stringResource(R.string.summary_analyzing),
                                color = GlassTokens.TextSecondary,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(start = 16.dp),
                            )
                        }
                    }
                }

                uiState.analysisError?.let { error ->
                    item {
                        GlassPanel(modifier = Modifier.fillMaxWidth(), fill = GlassTokens.Destructive) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(errorMessage(error), color = GlassTokens.TextPrimary, fontSize = 14.sp)
                                GlassPrimaryButton(
                                    text = stringResource(R.string.summary_retry),
                                    onClick = viewModel::retryAnalysis,
                                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                                )
                            }
                        }
                    }
                }

                uiState.summary?.let { summary ->
                    item {
                        GlassPanel(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = stringResource(R.string.summary_feedback_header),
                                    color = GlassTokens.TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                )
                                Text(
                                    text = summary,
                                    color = GlassTokens.TextSecondary,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(top = 6.dp),
                                )
                            }
                        }
                    }
                }

                if (uiState.errors.isNotEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.summary_errors_header, uiState.errors.size),
                            color = GlassTokens.TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                        )
                    }
                    items(uiState.errors, key = { it.id }) { error ->
                        ErrorCard(error)
                    }
                    item {
                        GlassPrimaryButton(
                            text = stringResource(R.string.summary_go_practice),
                            onClick = onGoToExercises,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                } else if (uiState.isAnalyzed) {
                    item {
                        Text(
                            text = stringResource(R.string.summary_no_errors),
                            color = GlassTokens.TextPrimary,
                            fontSize = 15.sp,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ErrorCard(error: UserError) {
    GlassPanel(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = categoryLabel(error.category),
                color = GlassTokens.Accent,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
            )
            Text(
                text = "„${error.original}”",
                color = GlassTokens.TextPrimary,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 4.dp),
            )
            Text(
                text = "→ ${error.corrected}",
                color = GlassTokens.Success,
                fontSize = 14.sp,
            )
            Text(
                text = error.explanation,
                color = GlassTokens.TextTertiary,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@Composable
private fun categoryLabel(category: ErrorCategory): String = stringResource(
    when (category) {
        ErrorCategory.GRAMMAR -> R.string.category_grammar
        ErrorCategory.VOCABULARY -> R.string.category_vocabulary
        ErrorCategory.PRONUNCIATION -> R.string.category_pronunciation
        ErrorCategory.FLUENCY -> R.string.category_fluency
    },
)
