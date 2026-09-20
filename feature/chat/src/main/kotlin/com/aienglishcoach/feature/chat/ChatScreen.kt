package com.aienglishcoach.feature.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import com.aienglishcoach.core.designsystem.glass.AuroraBackground
import com.aienglishcoach.core.designsystem.glass.GlassIconCircle
import com.aienglishcoach.core.designsystem.glass.GlassPanel
import com.aienglishcoach.core.designsystem.glass.GlassTokens
import com.aienglishcoach.core.designsystem.glass.ScreenHeading
import com.aienglishcoach.core.domain.model.Message
import com.aienglishcoach.core.domain.model.MessageRole

/**
 * Text chat with the tutor — glass bubbles over the aurora background.
 * Backed by the same [com.aienglishcoach.core.domain.usecase.conversation.SendMessageUseCase]
 * as the voice flow, so replies are real AI responses, not scripted text.
 */
@Composable
fun ChatScreen(viewModel: ChatViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AuroraBackground()
        Scaffold(containerColor = Color.Transparent) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = GlassTokens.ScreenSidePadding),
            ) {
                ScreenHeading(
                    title = stringResource(R.string.chat_title),
                    modifier = Modifier.padding(top = 24.dp, bottom = 16.dp),
                )

                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(uiState.messages, key = { it.id }) { message ->
                        ChatBubble(message)
                    }
                    if (uiState.isStarting) {
                        item {
                            CircularProgressIndicator(color = GlassTokens.Accent)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    OutlinedTextField(
                        value = uiState.input,
                        onValueChange = viewModel::onInputChanged,
                        modifier = Modifier.weight(1f),
                        placeholder = { Text(stringResource(R.string.chat_input_hint)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = GlassTokens.TextPrimary,
                            unfocusedTextColor = GlassTokens.TextPrimary,
                            focusedBorderColor = GlassTokens.PanelBorderStrong,
                            unfocusedBorderColor = GlassTokens.PanelBorder,
                            focusedPlaceholderColor = GlassTokens.TextTertiary,
                            unfocusedPlaceholderColor = GlassTokens.TextTertiary,
                            cursorColor = GlassTokens.Accent,
                        ),
                    )
                    GlassIconCircle(
                        size = 48.dp,
                        background = GlassTokens.AccentGlassFill,
                        onClick = viewModel::send,
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Send,
                            contentDescription = stringResource(R.string.chat_send),
                            tint = GlassTokens.TextPrimary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(message: Message) {
    val isUser = message.role == MessageRole.USER
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
    ) {
        GlassPanel(
            fill = if (isUser) GlassTokens.AccentGlassSoft else GlassTokens.PanelFill,
            modifier = Modifier.widthIn(max = 280.dp),
        ) {
            Text(
                text = message.content,
                color = GlassTokens.TextPrimary,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            )
        }
    }
}
