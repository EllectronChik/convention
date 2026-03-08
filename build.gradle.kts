plugins {
    `kotlin-dsl`
    id("maven-publish")
}

group = "dev.ellectronchik"
version = "1.0"

repositories {
    mavenCentral()
    google()
}

kotlin {
    jvmToolchain(21)
}

tasks.withType<Test> {
    useJUnitPlatform()
}

gradlePlugin {
    plugins {
        register("versioning") {
            id = "dev.ellectronchik.versioning"
            implementationClass = "dev.ellectronchik.convention.versioning.ProjectVersionPlugin"
        }

        register("publishingConfig") {
            id = "dev.ellectronchik.publishing.config"
            implementationClass = "dev.ellectronchik.convention.publishing.CorePublishingPlugin"
        }

        register("publishingModule") {
            id = "dev.ellectronchik.publishing"
            implementationClass = "dev.ellectronchik.convention.publishing.ModulePublishingPlugin"
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

dependencies {
    compileOnly(gradleApi())

    // Compile against AGP APIs without bundling them into the plugin artifact.
    compileOnly("com.android.tools.build:gradle-api:8.13.0")
    compileOnly("com.android.tools.build:gradle:8.13.0")

    // Compile against Kotlin Gradle Plugin APIs used by the convention plugins.
    compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin:2.0.0")

    testImplementation(gradleTestKit())
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testImplementation("com.google.truth:truth:1.1.5")

    // Make external plugin classes available to functional TestKit builds.
    testKitPlugins("com.android.tools.build:gradle-api:8.13.0")
    testKitPlugins("com.android.tools.build:gradle:8.13.0")
    testKitPlugins("org.jetbrains.kotlin:kotlin-gradle-plugin:2.0.0")
}
