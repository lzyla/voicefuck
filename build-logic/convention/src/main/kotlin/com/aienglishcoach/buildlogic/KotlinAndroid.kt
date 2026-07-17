package com.aienglishcoach.buildlogic

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension
import org.gradle.kotlin.dsl.configure

/**
 * Baseline Android + Kotlin configuration shared by the application and all
 * Android library modules: SDK levels, Java 17 toolchain and compiler flags.
 */
internal fun Project.configureKotlinAndroid(
    commonExtension: CommonExtension<*, *, *, *, *, *>,
) {
    commonExtension.apply {
        compileSdk = AndroidConfig.COMPILE_SDK

        defaultConfig {
            minSdk = AndroidConfig.MIN_SDK
        }

        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
        }
    }

    extensions.configure<KotlinAndroidProjectExtension> {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
            freeCompilerArgs.addAll(
                // Allow using kotlinx.coroutines Flow operators marked as experimental.
                "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
                // Project-wide opt-ins for widely used, stable-in-practice Compose APIs.
                "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
                "-opt-in=androidx.compose.foundation.ExperimentalFoundationApi",
                "-opt-in=androidx.compose.foundation.layout.ExperimentalLayoutApi",
            )
        }
    }
}

/** Central place for SDK versions so every module stays in sync. */
object AndroidConfig {
    const val COMPILE_SDK = 35
    const val TARGET_SDK = 35
    const val MIN_SDK = 26
}
