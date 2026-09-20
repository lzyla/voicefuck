package com.aienglishcoach.feature.learningpath

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

/**
 * In-memory record of which [LEARNING_PATH] lessons are done and how much XP
 * they earned. Deliberately not persisted to Room/DataStore: the curriculum
 * itself is static sample content (see [LearningPathData]), so progress on
 * it resets when the process dies rather than pretending to be durable.
 */
@Singleton
class LearningPathProgressStore @Inject constructor() {

    private val _completedLessonIds = MutableStateFlow<Set<String>>(emptySet())
    val completedLessonIds: StateFlow<Set<String>> = _completedLessonIds.asStateFlow()

    private val _totalXp = MutableStateFlow(0)
    val totalXp: StateFlow<Int> = _totalXp.asStateFlow()

    fun markCompleted(lessonId: String, xpReward: Int) {
        if (lessonId in _completedLessonIds.value) return
        _completedLessonIds.update { it + lessonId }
        _totalXp.update { it + xpReward }
    }
}
