package com.aienglishcoach.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.aienglishcoach.MainActivity
import com.aienglishcoach.R
import com.aienglishcoach.core.domain.repository.ExerciseRepository
import com.aienglishcoach.core.domain.repository.SettingsRepository
import com.aienglishcoach.core.domain.repository.VocabularyRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock

/**
 * Daily check for due revisions. Posts a gentle reminder notification when
 * something is due and the user has reminders enabled. Fully offline.
 */
@HiltWorker
class RevisionReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val exerciseRepository: ExerciseRepository,
    private val vocabularyRepository: VocabularyRepository,
    private val settingsRepository: SettingsRepository,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val preferences = settingsRepository.preferences.first()
        if (!preferences.revisionRemindersEnabled) return Result.success()

        val now = Clock.System.now()
        val dueExercises = exerciseRepository.observeDueCount(now).first()
        val dueVocabulary = vocabularyRepository.observeDueCount(now).first()
        val total = dueExercises + dueVocabulary
        if (total > 0 && NotificationManagerCompat.from(applicationContext).areNotificationsEnabled()) {
            postNotification(total)
        }
        return Result.success()
    }

    private fun postNotification(dueCount: Int) {
        val manager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                applicationContext.getString(R.string.notification_channel_revisions),
                NotificationManager.IMPORTANCE_DEFAULT,
            ),
        )

        val contentIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            Intent(applicationContext, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(applicationContext.getString(R.string.notification_revisions_title))
            .setContentText(
                applicationContext.getString(R.string.notification_revisions_text, dueCount),
            )
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .build()

        manager.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        const val WORK_NAME = "daily_revision_reminder"
        private const val CHANNEL_ID = "revisions"
        private const val NOTIFICATION_ID = 1001
    }
}
