package com.aienglishcoach

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aienglishcoach.core.designsystem.theme.CoachTheme
import com.aienglishcoach.core.domain.model.ThemeMode
import com.aienglishcoach.navigation.CoachApp
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val viewModel: MainViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            // Hold the splash until we know whether onboarding is needed.
            splashScreen.setKeepOnScreenCondition { uiState is MainUiState.Loading }

            when (val state = uiState) {
                MainUiState.Loading -> Unit
                is MainUiState.Ready -> {
                    val darkTheme = when (state.theme) {
                        ThemeMode.SYSTEM -> isSystemInDarkTheme()
                        ThemeMode.LIGHT -> false
                        ThemeMode.DARK -> true
                    }
                    CoachTheme(
                        darkTheme = darkTheme,
                        dynamicColor = state.useDynamicColor,
                    ) {
                        CoachApp(startWithOnboarding = !state.onboardingCompleted)
                    }
                }
            }
        }
    }
}
