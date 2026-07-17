package com.aienglishcoach.core.domain.service

/**
 * Port for deferring work to the platform scheduler (WorkManager in `:app`).
 * Keeps feature modules and use cases free of WorkManager dependencies.
 */
interface BackgroundScheduler {

    /** Consolidates AI long-term memory after the given conversation. */
    fun scheduleMemoryConsolidation(conversationId: Long)

    /** Daily check for due revisions; posts a reminder notification. */
    fun scheduleDailyRevisionReminder()
}
