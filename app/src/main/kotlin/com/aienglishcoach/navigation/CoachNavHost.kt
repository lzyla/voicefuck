package com.aienglishcoach.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.aienglishcoach.core.designsystem.glass.GlassBottomNav
import com.aienglishcoach.core.designsystem.glass.GlassNavItem
import com.aienglishcoach.feature.chat.ChatScreen
import com.aienglishcoach.feature.conversation.ConversationScreen
import com.aienglishcoach.feature.conversation.ConversationViewModel
import com.aienglishcoach.feature.conversation.list.ConversationListScreen
import com.aienglishcoach.feature.conversation.summary.ConversationSummaryScreen
import com.aienglishcoach.feature.home.HomeScreen
import com.aienglishcoach.feature.learningpath.LearningPathScreen
import com.aienglishcoach.feature.learningpath.LessonScreen
import com.aienglishcoach.feature.learningpath.LessonViewModel
import com.aienglishcoach.feature.onboarding.OnboardingScreen
import com.aienglishcoach.feature.practice.PracticeHubScreen
import com.aienglishcoach.feature.practice.exercises.ExerciseSessionScreen
import com.aienglishcoach.feature.practice.notes.VoiceNotesScreen
import com.aienglishcoach.feature.practice.pronunciation.PronunciationScreen
import com.aienglishcoach.feature.practice.vocabulary.VocabularyReviewScreen
import com.aienglishcoach.feature.practice.vocabulary.VocabularyScreen
import com.aienglishcoach.feature.profile.PaywallScreen
import com.aienglishcoach.feature.profile.ProfileScreen
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
    const val PATH = "path"
    const val LESSON = "path/lesson/{lessonId}"
    const val CHAT = "chat"
    const val PROFILE = "profile"
    const val PAYWALL = "paywall"

    fun conversation(id: Long) = "conversation/$id"
    fun conversationSummary(id: Long) = "conversation/$id/summary"
    fun lesson(id: String) = "path/lesson/$id"
}

private enum class TopLevelDestination(val route: String) {
    HOME(Routes.HOME),
    PATH(Routes.PATH),
    PRACTICE(Routes.PRACTICE),
    CHAT(Routes.CHAT),
    PROFILE(Routes.PROFILE),
}

/** Root composable: floating glass bottom navigation + navigation graph. */
@Composable
fun CoachApp(startWithOnboarding: Boolean) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val showBottomBar = currentRoute in TopLevelDestination.entries.map { it.route }

    NavHost(
        navController = navController,
        startDestination = if (startWithOnboarding) Routes.ONBOARDING else Routes.HOME,
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
            CoachScaffold(showBottomBar, currentRoute, navController) { padding ->
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
                    onOpenConversations = { navController.navigate(Routes.CONVERSATIONS) },
                )
            }
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

        composable(Routes.PATH) {
            CoachScaffold(showBottomBar, currentRoute, navController) {
                LearningPathScreen(
                    onOpenLesson = { lessonId -> navController.navigate(Routes.lesson(lessonId)) },
                )
            }
        }

        composable(
            route = Routes.LESSON,
            arguments = listOf(navArgument(LessonViewModel.ARG_LESSON_ID) { type = NavType.StringType }),
        ) {
            LessonScreen(
                onBack = { navController.popBackStack() },
                onLessonFinished = { navController.popBackStack() },
            )
        }

        composable(Routes.PRACTICE) {
            CoachScaffold(showBottomBar, currentRoute, navController) {
                PracticeHubScreen(
                    onOpenExercises = { navController.navigate(Routes.PRACTICE_EXERCISES) },
                    onOpenVocabulary = { navController.navigate(Routes.PRACTICE_VOCABULARY) },
                    onOpenPronunciation = {
                        navController.navigate(Routes.PRACTICE_PRONUNCIATION)
                    },
                    onOpenNotes = { navController.navigate(Routes.PRACTICE_NOTES) },
                )
            }
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

        composable(Routes.CHAT) {
            CoachScaffold(showBottomBar, currentRoute, navController) {
                ChatScreen()
            }
        }

        composable(Routes.PROFILE) {
            CoachScaffold(showBottomBar, currentRoute, navController) {
                ProfileScreen(
                    onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                    onOpenPaywall = { navController.navigate(Routes.PAYWALL) },
                    onOpenStatistics = { navController.navigate(Routes.STATISTICS) },
                )
            }
        }

        composable(Routes.PAYWALL) {
            PaywallScreen(onClose = { navController.popBackStack() })
        }

        composable(Routes.STATISTICS) {
            StatisticsScreen()
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}

/**
 * Top-level destinations render their own full-bleed background, so the
 * floating [GlassBottomNav] is overlaid via a plain [Box] rather than a
 * Material [androidx.compose.material3.Scaffold] bottom bar slot (which
 * would paint an opaque surface behind it).
 */
@Composable
private fun CoachScaffold(
    showBottomBar: Boolean,
    currentRoute: String?,
    navController: NavHostController,
    content: @Composable (PaddingValues) -> Unit,
) {
    Box(modifier = Modifier) {
        content(PaddingValues(bottom = if (showBottomBar) 84.dp else 0.dp))
        if (showBottomBar) {
            GlassBottomNav(
                items = bottomNavItems(),
                currentRoute = currentRoute,
                onNavigate = { item ->
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
            )
        }
    }
}

private fun bottomNavItems(): List<GlassNavItem> = listOf(
    GlassNavItem("Home", Icons.Rounded.Home, Routes.HOME),
    GlassNavItem("Path", Icons.Rounded.School, Routes.PATH),
    GlassNavItem("Practice", Icons.Rounded.SelfImprovement, Routes.PRACTICE),
    GlassNavItem("Chat", Icons.AutoMirrored.Rounded.Chat, Routes.CHAT),
    GlassNavItem("Profile", Icons.Rounded.AccountCircle, Routes.PROFILE),
)
