plugins {
    alias(libs.plugins.aienglishcoach.android.library)
    alias(libs.plugins.aienglishcoach.android.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.aienglishcoach.core.network"
    buildFeatures.buildConfig = true

    defaultConfig {
        buildConfigField("String", "OPENAI_BASE_URL", "\"https://api.openai.com/\"")
    }
}

dependencies {
    implementation(projects.core.common)

    implementation(libs.retrofit.core)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.okhttp.core)
    implementation(libs.okhttp.logging)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.timber)

    testImplementation(libs.okhttp.mockwebserver)
}
