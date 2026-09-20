package com.aienglishcoach.core.domain.model

import kotlinx.datetime.Instant

/** A word or phrase the learner is acquiring, with its SRS schedule. */
data class VocabularyItem(
    val id: Long = 0,
    val word: String,
    val translation: String,
    val definition: String? = null,
    val example: String? = null,
    /** Conversation the word first appeared in; null when added manually. */
    val sourceConversationId: Long? = null,
    val status: VocabularyStatus = VocabularyStatus.NEW,
    val srs: SrsState,
    val createdAt: Instant,
)

enum class VocabularyStatus { NEW, LEARNING, MASTERED }
