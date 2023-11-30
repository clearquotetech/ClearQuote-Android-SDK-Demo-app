pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        mavenLocal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        mavenLocal()
        maven { url = uri("https://jitpack.io") }
        maven { url = uri("https://europe-maven.pkg.dev/anyline-ttr-sdk/maven") }
    }
}

rootProject.name = "ClearQuote-SDK-Showcase-app"
include(":app")
 