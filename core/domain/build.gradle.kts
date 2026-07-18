plugins {
    alias(libs.plugins.aienglishcoach.jvm.library)
}

dependencies {
    api(projects.core.common)
    api(libs.kotlinx.datetime)
    implementation("javax.inject:javax.inject:1")
}
