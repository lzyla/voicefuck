plugins {
    alias(libs.plugins.aienglishcoach.android.feature)
}

android {
    namespace = "com.aienglishcoach.feature.practice"
}

dependencies {
    testImplementation(projects.core.testing)
}
