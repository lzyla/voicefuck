package com.aienglishcoach.core.domain.model

import kotlinx.datetime.Instant

/** A voice memo recorded by the learner, optionally transcribed. */
data class VoiceNote(
    val id: Long = 0,
    val title: String,
    val audioPath: String,
    val transcription: String? = null,
    val durationSeconds: Int,
    val createdAt: Instant,
)
