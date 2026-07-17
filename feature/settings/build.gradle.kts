plugins {
    alias(libs.plugins.aienglishcoach.android.feature)
}

android {
    namespace = "com.aienglishcoach.feature.settings"
}

dependencies {
    testImplementation(projects.core.testing)
}
