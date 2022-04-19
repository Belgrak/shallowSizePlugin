import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompile

val jvmTargetVersion: String by project

plugins {
    kotlin("jvm")
    java
    id("io.gitlab.arturbosch.detekt") version "1.19.0"
}

group = "spbu.kotlin.class.shallowSize"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies{
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.19.0")
}

detekt {
    source = files("shallowSizePlugin/src/main/kotlin", "shalowSizePluginTest/src/test/kotlin")
    autoCorrect = true
}

allprojects {
    apply {
        plugin("kotlin")
    }

    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven {
            url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
        }
    }

    tasks.withType<KotlinJvmCompile> {
        kotlinOptions {
            jvmTarget = jvmTargetVersion
            languageVersion = "1.5"
            apiVersion = "1.5"
        }
    }
}
