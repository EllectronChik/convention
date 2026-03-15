package dev.ellectronchik.convention.publishing.internal.extensions

import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register

internal fun Project.ensureDokkaJar(): TaskProvider<Jar> {
    pluginManager.apply("org.jetbrains.dokka")

    val existingTask = tasks.findByName("dokkaJavadocJar")

    return if (existingTask != null) {
        tasks.named<Jar>("dokkaJavadocJar")
    } else {
        tasks.register<Jar>("dokkaJavadocJar") {
            group = "documentation"
            description = "Assembles a jar archive containing the Dokka Javadoc."
            archiveClassifier.set("javadoc")
            from(tasks.named("dokkaJavadoc"))
        }
    }
}
