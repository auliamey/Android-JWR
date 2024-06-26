// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        mavenCentral()
        maven (url = "https://jitpack.io")
    }

    dependencies {
        val navVersion = "2.7.7"
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:$navVersion")
        classpath("com.google.android.gms:play-services-maps:18.2.0")



//        val androidPluginVersion ="8.3.1"
//
//        classpath("com.android.tools.build:gradle:$androidPluginVersion")
    }
}


plugins {
    id ("com.android.application") version "8.2.2" apply false
    id ("com.android.library") version "7.3.1" apply false
    id ("org.jetbrains.kotlin.android") version "1.9.22" apply false
    id("com.google.devtools.ksp") version "1.9.0-1.0.13" apply false
}