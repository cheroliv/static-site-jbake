pluginManagement {
    buildscript {
        repositories {
            gradlePluginPortal()
            mavenCentral()
        }
    }
    plugins { id("org.jbake.site").version(extra["jbake_gradle_plugin_version"].toString()) }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "static-site-jbake"