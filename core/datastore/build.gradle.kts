plugins {
    alias(libs.plugins.aienglishcoach.android.library)
    alias(libs.plugins.aienglishcoach.android.hilt)
}

android {
    namespace = "com.aienglishcoach.core.datastore"
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.domain)

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.security.crypto)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.timber)
}
