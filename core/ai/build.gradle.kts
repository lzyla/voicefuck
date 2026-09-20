plugins {
    alias(libs.plugins.aienglishcoach.android.library)
    alias(libs.plugins.aienglishcoach.android.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.aienglishcoach.core.ai"
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.domain)
    implementation(projects.core.network)

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.timber)
}
