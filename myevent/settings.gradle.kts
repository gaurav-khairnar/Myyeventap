pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS) // Changed to allow project repos if needed
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "myevent"
include(":app")