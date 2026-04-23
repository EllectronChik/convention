plugins {
    `kotlin-dsl`
    id("maven-publish")
    alias(libs.plugins.gradle.plugin.publish)
}

group = "dev.ellectronchik"
version = "1.0.1"

repositories {
    mavenCentral()
    google()
}

kotlin {
    jvmToolchain(17)
}

tasks.withType<Test> {
    useJUnitPlatform()

    if (project.hasProperty("testGradleVersion")) {
        systemProperty("testGradleVersion", project.property("testGradleVersion").toString())
    }

    if (project.hasProperty("testAgpVersion")) {
        systemProperty("testAgpVersion", project.property("testAgpVersion").toString())
    }
    if (project.hasProperty("testKgpVersion")) {
        systemProperty("testKgpVersion", project.property("testKgpVersion").toString())
    }
}

gradlePlugin {
    website.set("https://github.com/ellectronchik/convention")
    vcsUrl.set("https://github.com/ellectronchik/convention.git")

    plugins {
        register("versioningConfig") {
            id = "dev.ellectronchik.versioning.config"
            displayName = "Convention Versioning Config"
            description = "Declares shared versioning defaults for the entire multi-module project."
            implementationClass = "dev.ellectronchik.convention.versioning.CoreProjectVersionPlugin"
            tags.set(listOf("maven", "versioning", "convention"))
        }

        register("versioningModule") {
            id = "dev.ellectronchik.versioning"
            displayName = "Convention Versioning Plugin"
            description = "Propagates version settings to Kotlin JVM and Android modules automatically."
            implementationClass = "dev.ellectronchik.convention.versioning.ProjectVersionPlugin"
            tags.set(listOf("kotlin", "android", "versioning", "convention"))
        }

        register("publishingConfig") {
            id = "dev.ellectronchik.publishing.config"
            displayName = "Convention Publishing Config"
            description = "Declares shared publishing defaults for the entire multi-module project."
            implementationClass = "dev.ellectronchik.convention.publishing.CorePublishingPlugin"
            tags.set(listOf("publishing", "maven", "convention"))
        }

        register("publishingModule") {
            id = "dev.ellectronchik.publishing"
            displayName = "Convention Module Publishing"
            description = "Configures maven-publish for individual Kotlin and Android modules."
            tags.set(listOf("publishing", "maven", "android", "kotlin"))
            implementationClass = "dev.ellectronchik.convention.publishing.ModulePublishingPlugin"
        }
    }
}

publishing {
    publications.withType<MavenPublication> {
        pom {
            licenses {
                license {
                    name.set("The MIT License")
                    url.set("https://opensource.org/licenses/MIT")
                    distribution.set("repo")
                }
            }

            developers {
                developer {
                    id.set("ellectronchik")
                    name.set("Yan Novak")
                    email.set("ellectronchik@proton.me")
                }
            }
        }
    }
}

// Additional plugin dependencies that must be available in Gradle TestKit test builds.
val testKitPlugins: Configuration by configurations.creating

tasks.named<PluginUnderTestMetadata>("pluginUnderTestMetadata") {
    // Extend the generated plugin-under-test metadata so TestKit test projects
    // can resolve Android and Kotlin plugin classes used by the convention plugins.
    pluginClasspath.from(testKitPlugins)
}

val testAgpVersion = project.findProperty("testAgpVersion") as String? ?: "8.13.2"
val testKgpVersion = project.findProperty("testKgpVersion") as String? ?: "2.3.0"

dependencies {
    compileOnly(gradleApi())

    implementation(libs.dokka.gradle.plugin)

    // Compile against AGP APIs without bundling them into the plugin artifact.
    compileOnly(libs.android.gradle.api)
    compileOnly(libs.android.gradle)

    // Compile against Kotlin Gradle Plugin APIs used by the convention plugins.
    compileOnly(libs.kotlin.gradle.plugin)

    testImplementation(gradleTestKit())
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.google.truth)

    // Make external plugin classes available to functional TestKit builds.
    testKitPlugins("com.android.tools.build:gradle-api:$testAgpVersion")
    testKitPlugins("com.android.tools.build:gradle:$testAgpVersion")
    testKitPlugins("org.jetbrains.kotlin:kotlin-gradle-plugin:$testKgpVersion")
}
