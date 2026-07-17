package com.aienglishcoach.feature.conversation.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aienglishcoach.core.designsystem.component.CoachCard
import com.aienglishcoach.core.designsystem.component.EmptyState
import com.aienglishcoach.core.designsystem.component.LoadingIndicator
import com.aienglishcoach.core.designsystem.theme.Spacing
import com.aienglishcoach.core.domain.model.Conversation
import com.aienglishcoach.core.domain.model.ConversationStatus
import com.aienglishcoach.feature.conversation.R
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/** Conversation history; each entry opens its summary (or resumes viewing). */
@Composable
fun ConversationListScreen(
    onStartConversation: () -> Unit,
    onOpenConversation: (Long) -> Unit,
    viewModel: ConversationListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.conversation_list_title)) })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onStartConversation) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = stringResource(R.string.conversation_list_new),
                )
            }
        },
    ) { padding ->
        when (val state = uiState) {
            ConversationListUiState.Loading ->
                LoadingIndicator(modifier = Modifier.padding(padding))

            is ConversationListUiState.Ready -> if (state.conversations.isEmpty()) {
                EmptyState(
                    icon = Icons.Rounded.ChatBubbleOutline,
                    title = stringResource(R.string.conversation_list_empty_title),
                    description = stringResource(R.string.conversation_list_empty_description),
                    actionLabel = stringResource(R.string.conversation_list_new),
                    onAction = onStartConversation,
                    modifier = Modifier.padding(padding).fillMaxSize(),
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(Spacing.md),
                    verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                ) {
                    items(state.conversations, key = { it.id }) { conversation ->
                        ConversationRow(
                            conversation = conversation,
                            onClick = { onOpenConversation(conversation.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ConversationRow(conversation: Conversation, onClick: () -> Unit) {
    CoachCard(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(Spacing.md)) {
            Text(
                text = conversation.title,
                style = MaterialTheme.typography.titleMedium,
            )
            val localDate = conversation.startedAt
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .date
            Text(
                text = stringResource(
                    R.string.conversation_list_meta,
                    localDate.toString(),
                    conversation.durationSeconds / 60,
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (conversation.status == ConversationStatus.ANALYZED && conversation.summary != null) {
                Text(
                    text = conversation.summary.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = Spacing.xxs),
                    maxLines = 2,
                )
            }
        }
    }
}
