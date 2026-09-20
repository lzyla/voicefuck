import androidx.room.gradle.RoomExtension
import com.aienglishcoach.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

/**
 * Applies Room with KSP and enables schema export (schemas are versioned in
 * the module's `schemas/` directory so migrations can be tested).
 */
class AndroidRoomConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("androidx.room")
                apply("com.google.devtools.ksp")
            }

            extensions.configure<RoomExtension> {
                schemaDirectory("$projectDir/schemas")
            }

            dependencies {
                // Room entities/DAOs/RoomDatabase subclasses are part of this
                // module's public API (e.g. CoachDatabase extends RoomDatabase),
                // so downstream modules need these types on their classpath too.
                "api"(libs.findLibrary("androidx-room-runtime").get())
                "api"(libs.findLibrary("androidx-room-ktx").get())
                "ksp"(libs.findLibrary("androidx-room-compiler").get())
            }
        }
    }
}
