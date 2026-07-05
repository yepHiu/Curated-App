enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "curated-app"

include(":app:phone")
include(":core")
include(":data")
include(":player:core")
include(":player:local")
include(":setup")
include(":modes:film")
include(":settings")

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }
}
