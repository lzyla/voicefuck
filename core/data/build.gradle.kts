plugins {
    alias(libs.plugins.aienglishcoach.android.library)
    alias(libs.plugins.aienglishcoach.android.hilt)
}

android {
    namespace = "com.aienglishcoach.core.data"
}

dependencies {
    api(projects.core.domain)
    implementation(projects.core.common)
    implementation(projects.core.database)
    implementation(projects.core.datastore)
    implementation(projects.core.network)

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.timber)
}
