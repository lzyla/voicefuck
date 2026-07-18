plugins {
    alias(libs.plugins.aienglishcoach.android.library.compose)
}

android {
    namespace = "com.aienglishcoach.core.designsystem"
}

dependencies {
    api(libs.androidx.compose.material.icons.extended)
}
