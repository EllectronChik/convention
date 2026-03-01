package dev.ellectronchik.convention.publishing

import dev.ellectronchik.convention.publishing.dsl.CorePublishingExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class CorePublishingPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.project.extensions.create(
            "corePublishing",
            CorePublishingExtension::class.java,
        )
    }
}
