package com.aienglishcoach.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.aienglishcoach.R
import com.aienglishcoach.feature.conversation.ConversationScreen
import com.aienglishcoach.feature.conversation.ConversationViewModel
import com.aienglishcoach.feature.conversation.list.ConversationListScreen
import com.aienglishcoach.feature.conversation.summary.ConversationSummaryScreen
import com.aienglishcoach.feature.home.HomeScreen
import com.aienglishcoach.feature.onboarding.OnboardingScreen
import com.aienglishcoach.feature.practice.PracticeHubScreen
import com.aienglishcoach.feature.practice.exercises.ExerciseSessionScreen
import com.aienglishcoach.feature.practice.notes.VoiceNotesScreen
import com.aienglishcoach.feature.practice.pronunciation.PronunciationScreen
import com.aienglishcoach.feature.practice.vocabulary.VocabularyReviewScreen
import com.aienglishcoach.feature.practice.vocabulary.VocabularyScreen
import com.aienglishcoach.feature.settings.SettingsScreen
import com.aienglishcoach.feature.statistics.StatisticsScreen

/** All navigation routes of the app in one place. */
object Routes {
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val CONVERSATIONS = "conversations"
    const val CONVERSATION = "conversation/{conversationId}"
    const val CONVERSATION_SUMMARY = "conversation/{conversationId}/summary"
    const val PRACTICE = "practice"
    const val PRACTICE_EXERCISES = "practice/exercises"
    const val PRACTICE_VOCABULARY = "practice/vocabulary"
    const val PRACTICE_VOCABULARY_REVIEW = "practice/vocabulary/review"
    const val PRACTICE_PRONUNCIATION = "practice/pronunciation"
    const val PRACTICE_NOTES = "practice/notes"
    const val STATISTICS = "statistics"
    const val SETTINGS = "settings"

    fun conversation(id: Long) = "conversation/$id"
    fun conversationSummary(id: Long) = "conversation/$id/summary"
}

private enum class TopLevelDestination(val route: String) {
    HOME(Routes.HOME),
    CONVERSATIONS(Routes.CONVERSATIONS),
    PRACTICE(Routes.PRACTICE),
    STATISTICS(Routes.STATISTICS),
}

/** Root composable: bottom navigation scaffold + navigation graph. */
@Composable
fun CoachApp(startWithOnboarding: Boolean) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val showBottomBar = currentRoute in TopLevelDestination.entries.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                CoachBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { destination ->
                        navController.navigate(destination.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = if (startWithOnboarding) Routes.ONBOARDING else Routes.HOME,
            modifier = Modifier.padding(padding),
        ) {
            composable(Routes.ONBOARDING) {
                OnboardingScreen(
                    onFinished = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.ONBOARDING) { inclusive = true }
                        }
                    },
                )
            }

            composable(Routes.HOME) {
                HomeScreen(
                    onStartConversation = {
                        navController.navigate(
                            Routes.conversation(ConversationViewModel.NEW_CONVERSATION_ID),
                        )
                    },
                    onOpenExercises = { navController.navigate(Routes.PRACTICE_EXERCISES) },
                    onOpenVocabularyReview = {
                        navController.navigate(Routes.PRACTICE_VOCABULARY_REVIEW)
                    },
                    onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                )
            }

            composable(Routes.CONVERSATIONS) {
                ConversationListScreen(
                    onStartConversation = {
                        navController.navigate(
                            Routes.conversation(ConversationViewModel.NEW_CONVERSATION_ID),
                        )
                    },
                    onOpenConversation = { id ->
                        navController.navigate(Routes.conversationSummary(id))
                    },
                )
            }

            composable(
                route = Routes.CONVERSATION,
                arguments = listOf(
                    navArgument("conversationId") { type = NavType.LongType },
                ),
            ) {
                ConversationScreen(
                    onBack = { navController.popBackStack() },
                    onConversationEnded = { id ->
                        navController.navigate(Routes.conversationSummary(id)) {
                            popUpTo(Routes.CONVERSATION) { inclusive = true }
                        }
                    },
                )
            }

            composable(
                route = Routes.CONVERSATION_SUMMARY,
                arguments = listOf(
                    navArgument("conversationId") { type = NavType.LongType },
                ),
            ) {
                ConversationSummaryScreen(
                    onBack = { navController.popBackStack() },
                    onGoToExercises = { navController.navigate(Routes.PRACTICE_EXERCISES) },
                )
            }

            composable(Routes.PRACTICE) {
                PracticeHubScreen(
                    onOpenExercises = { navController.navigate(Routes.PRACTICE_EXERCISES) },
                    onOpenVocabulary = { navController.navigate(Routes.PRACTICE_VOCABULARY) },
                    onOpenPronunciation = {
                        navController.navigate(Routes.PRACTICE_PRONUNCIATION)
                    },
                    onOpenNotes = { navController.navigate(Routes.PRACTICE_NOTES) },
                )
            }

            composable(Routes.PRACTICE_EXERCISES) {
                ExerciseSessionScreen(onBack = { navController.popBackStack() })
            }

            composable(Routes.PRACTICE_VOCABULARY) {
                VocabularyScreen(
                    onStartReview = {
                        navController.navigate(Routes.PRACTICE_VOCABULARY_REVIEW)
                    },
                )
            }

            composable(Routes.PRACTICE_VOCABULARY_REVIEW) {
                VocabularyReviewScreen(onBack = { navController.popBackStack() })
            }

            composable(Routes.PRACTICE_PRONUNCIATION) {
                PronunciationScreen()
            }

            composable(Routes.PRACTICE_NOTES) {
                VoiceNotesScreen()
            }

            composable(Routes.STATISTICS) {
                StatisticsScreen()
            }

            composable(Routes.SETTINGS) {
                SettingsScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}

@Composable
private fun CoachBottomBar(
    currentRoute: String?,
    onNavigate: (TopLevelDestination) -> Unit,
) {
    NavigationBar {
        TopLevelDestination.entries.forEach { destination ->
            NavigationBarItem(
                selected = currentRoute == destination.route,
                onClick = { onNavigate(destination) },
                icon = {
                    Icon(
                        imageVector = destination.icon(),
                        contentDescription = null,
                    )
                },
                label = { Text(stringResource(destination.labelRes())) },
            )
        }
    }
}

private fun TopLevelDestination.labelRes(): Int = when (this) {
    TopLevelDestination.HOME -> R.string.nav_home
    TopLevelDestination.CONVERSATIONS -> R.string.nav_conversations
    TopLevelDestination.PRACTICE -> R.string.nav_practice
    TopLevelDestination.STATISTICS -> R.string.nav_statistics
}

private fun TopLevelDestination.icon() = when (this) {
    TopLevelDestination.HOME -> Icons.Rounded.Home
    TopLevelDestination.CONVERSATIONS -> Icons.AutoMirrored.Rounded.Chat
    TopLevelDestination.PRACTICE -> Icons.Rounded.School
    TopLevelDestination.STATISTICS -> Icons.Rounded.BarChart
}
