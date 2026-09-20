package com.aienglishcoach.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.aienglishcoach.core.domain.model.EnglishLevel
import com.aienglishcoach.core.domain.model.LearningGoal
import com.aienglishcoach.core.domain.model.ThemeMode
import com.aienglishcoach.core.domain.model.TtsVoice
import com.aienglishcoach.core.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Preferences-DataStore backed storage for [UserPreferences].
 * The AI API key is intentionally NOT here — see [ApiKeyStore].
 */
@Singleton
class UserPreferencesDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {

    val preferences: Flow<UserPreferences> = dataStore.data.map(::toModel)

    suspend fun update(transform: (UserPreferences) -> UserPreferences) {
        dataStore.edit { mutable ->
            val updated = transform(toModel(mutable))
            mutable[Keys.ONBOARDING_COMPLETED] = updated.onboardingCompleted
            mutable[Keys.ENGLISH_LEVEL] = updated.englishLevel.name
            mutable[Keys.LEARNING_GOALS] = updated.learningGoals.map { it.name }.toSet()
            mutable[Keys.DAILY_GOAL_MINUTES] = updated.dailyGoalMinutes
            mutable[Keys.TTS_SPEECH_RATE] = updated.ttsSpeechRate
            mutable[Keys.TTS_VOICE] = updated.ttsVoice.name
            mutable[Keys.AI_MODEL] = updated.aiModel
            mutable[Keys.THEME] = updated.theme.name
            mutable[Keys.DYNAMIC_COLOR] = updated.useDynamicColor
            mutable[Keys.REVISION_REMINDERS] = updated.revisionRemindersEnabled
        }
    }

    suspend fun clear() {
        dataStore.edit { it.clear() }
    }

    private fun toModel(preferences: Preferences): UserPreferences = UserPreferences(
        onboardingCompleted = preferences[Keys.ONBOARDING_COMPLETED] ?: false,
        englishLevel = preferences[Keys.ENGLISH_LEVEL].toEnumOrDefault(EnglishLevel.B1),
        learningGoals = preferences[Keys.LEARNING_GOALS]
            .orEmpty()
            .mapNotNull { name -> LearningGoal.entries.find { it.name == name } }
            .toSet(),
        dailyGoalMinutes = preferences[Keys.DAILY_GOAL_MINUTES] ?: 10,
        ttsSpeechRate = preferences[Keys.TTS_SPEECH_RATE] ?: 1.0f,
        ttsVoice = preferences[Keys.TTS_VOICE].toEnumOrDefault(TtsVoice.US),
        aiModel = preferences[Keys.AI_MODEL] ?: UserPreferences.DEFAULT_AI_MODEL,
        theme = preferences[Keys.THEME].toEnumOrDefault(ThemeMode.SYSTEM),
        useDynamicColor = preferences[Keys.DYNAMIC_COLOR] ?: false,
        revisionRemindersEnabled = preferences[Keys.REVISION_REMINDERS] ?: true,
    )

    private inline fun <reified T : Enum<T>> String?.toEnumOrDefault(default: T): T =
        this?.let { name -> enumValues<T>().find { it.name == name } } ?: default

    private object Keys {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val ENGLISH_LEVEL = stringPreferencesKey("english_level")
        val LEARNING_GOALS = stringSetPreferencesKey("learning_goals")
        val DAILY_GOAL_MINUTES = intPreferencesKey("daily_goal_minutes")
        val TTS_SPEECH_RATE = floatPreferencesKey("tts_speech_rate")
        val TTS_VOICE = stringPreferencesKey("tts_voice")
        val AI_MODEL = stringPreferencesKey("ai_model")
        val THEME = stringPreferencesKey("theme")
        val DYNAMIC_COLOR = booleanPreferencesKey("use_dynamic_color")
        val REVISION_REMINDERS = booleanPreferencesKey("revision_reminders_enabled")
    }
}
