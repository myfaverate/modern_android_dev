@file:Suppress("UnstableApiUsage")

import org.gradle.kotlin.dsl.maven
import java.io.InputStream
import java.util.Properties


pluginManagement {
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
val properties: Properties = rootDir.resolve("gradle.properties").inputStream().use { inputStream: InputStream ->
    Properties().apply {
        load(inputStream)
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri(path = "https://maven.pkg.github.com/myfaverate/modern_android_dev")
            credentials {
                logger.lifecycle("properties: $properties")
                username = properties["gpr.user"] as String? ?: System.getenv("USERNAME")
                password = properties["gpr.key"] as String? ?: System.getenv("TOKEN")
            }
        }
    }
}

rootProject.name = "HelloKtorfit"
include(":app")
 