// oss-licenses-plugin predates the plugin-marker convention, so it is added the legacy
// way (buildscript classpath) instead of through the version catalog's plugins{} DSL.
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.google.android.gms:oss-licenses-plugin:0.13.0")
        // AGP 9's built-in Kotlin support bundles its own Kotlin Gradle Plugin version;
        // pin it explicitly so it matches the compose/serialization plugin versions below.
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.4.20")
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    // No org.jetbrains.kotlin.android plugin: AGP 9's built-in Kotlin support replaces it.
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.room) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.ktlint) apply false
}
