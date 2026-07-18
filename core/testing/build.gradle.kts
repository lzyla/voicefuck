plugins {
    alias(libs.plugins.aienglishcoach.jvm.library)
}

dependencies {
    implementation(projects.core.common)

    api(libs.junit)
    api(libs.kotlinx.coroutines.test)
    api(libs.mockk)
    api(libs.turbine)
}
