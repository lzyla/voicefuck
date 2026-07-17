pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "AIEnglishCoach"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(":app")

include(":core:common")
include(":core:designsystem")
include(":core:domain")
include(":core:database")
include(":core:datastore")
include(":core:network")
include(":core:data")
include(":core:audio")
include(":core:ai")
include(":core:testing")

include(":feature:onboarding")
include(":feature:home")
include(":feature:conversation")
include(":feature:practice")
include(":feature:statistics")
include(":feature:settings")
include(":feature:learningpath")
include(":feature:chat")
include(":feature:profile")
