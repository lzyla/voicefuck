import com.aienglishcoach.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * Convention for `:feature:*` modules: Compose library + Hilt + the standard
 * dependency set every screen needs (design system, domain, common, lifecycle,
 * navigation). Feature modules must never depend on other feature modules.
 */
class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("aienglishcoach.android.library.compose")
            pluginManager.apply("aienglishcoach.android.hilt")

            dependencies {
                "implementation"(project(":core:designsystem"))
                "implementation"(project(":core:domain"))
                "implementation"(project(":core:common"))

                "implementation"(libs.findLibrary("androidx-hilt-navigation-compose").get())
                "implementation"(libs.findLibrary("androidx-lifecycle-runtime-compose").get())
                "implementation"(libs.findLibrary("androidx-lifecycle-viewmodel-compose").get())
                "implementation"(libs.findLibrary("androidx-navigation-compose").get())
                "implementation"(libs.findLibrary("kotlinx-coroutines-android").get())
                "implementation"(libs.findLibrary("kotlinx-datetime").get())
                "implementation"(libs.findLibrary("timber").get())
            }
        }
    }
}
