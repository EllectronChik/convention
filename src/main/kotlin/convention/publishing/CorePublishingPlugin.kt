package dev.ellectronchik.convention.publishing

import dev.ellectronchik.convention.publishing.dsl.CorePublishingExtension
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Root-level plugin that provides shared publishing defaults via the `corePublishing` extension.
 *
 * This plugin must be applied only to the root project.
 */
class CorePublishingPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        if (target != target.rootProject) {
            throw GradleException("CorePublishingPlugin must be applied only to the root project")
        }

        target.project.extensions.create(
            "corePublishing",
            CorePublishingExtension::class.java,
        )
    }
}
