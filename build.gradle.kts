plugins {
    `kotlin-dsl`
    id("maven-publish")
}

group = "dev.ellectronchik"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    google()
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    compileOnly(gradleApi())
    compileOnly("com.android.tools.build:gradle:8.13.0")
}
