package dev.ellectronchik.convention.publishing

import dev.ellectronchik.convention.publishing.dsl.CorePublishingExtension
import dev.ellectronchik.convention.publishing.internal.constants.PluginProps
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Root-project plugin that registers the [dev.ellectronchik.convention.publishing.dsl.CorePublishingExtension]
 * DSL block used to configure shared publishing defaults for all subprojects.
 *
 * Apply this plugin **only to the root project** and configure it before any subproject consumes it:
 * ```kotlin
 * // root build.gradle.kts
 * plugins {
 *     id("dev.ellectronchik.publishing.config")
 * }
 *
 * corePublishing {
 *     groupId("com.example")
 *     repositories { mavenLocal() }
 * }
 * ```
 *
 * @throws org.gradle.api.GradleException if applied to any project other than the root project.
 * @see dev.ellectronchik.convention.publishing.ModulePublishingPlugin
 */
class CorePublishingPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        if (target != target.rootProject) {
            throw GradleException("Plugin `dev.ellectronchik.publishing.config` must be applied only to the root project")
        }

        target.project.extensions.create(
            PluginProps.CORE_EXTENSION_NAME,
            CorePublishingExtension::class.java,
        )
    }
}
