plugins {
    alias(libs.plugins.aienglishcoach.android.library)
    alias(libs.plugins.aienglishcoach.android.hilt)
}

android {
    namespace = "com.aienglishcoach.core.audio"
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.domain)

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.timber)
}
