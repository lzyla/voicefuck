plugins {
    alias(libs.plugins.aienglishcoach.android.library)
    alias(libs.plugins.aienglishcoach.android.room)
    alias(libs.plugins.aienglishcoach.android.hilt)
}

android {
    namespace = "com.aienglishcoach.core.database"
}

dependencies {
    implementation(projects.core.common)

    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.serialization.json)

    androidTestImplementation(libs.androidx.room.testing)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
}
