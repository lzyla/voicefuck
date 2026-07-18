plugins {
    alias(libs.plugins.aienglishcoach.jvm.library)
}

dependencies {
    api(libs.kotlinx.datetime)
    implementation("javax.inject:javax.inject:1")
}
