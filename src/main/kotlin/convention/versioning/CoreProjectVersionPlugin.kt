package dev.ellectronchik.convention.versioning

import dev.ellectronchik.convention.versioning.dsl.VersioningExtension
import dev.ellectronchik.convention.versioning.internal.constants.PluginProps
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Root-project plugin that registers the [dev.ellectronchik.convention.versioning.dsl.VersioningExtension]
 * DSL block used to configure a shared version across all subprojects.
 *
 * Apply this plugin **only to the root project** and configure it before any subproject consumes it:
 * ```kotlin
 * // root build.gradle.kts
 * plugins {
 *     id("dev.ellectronchik.versioning.config")
 * }
 *
 * versioning {
 *     currentVersionName("1.0.0")
 *     currentAndroidVersionCode(1)
 * }
 * ```
 *
 * @throws org.gradle.api.GradleException if applied to any project other than the root project.
 * @see dev.ellectronchik.convention.versioning.ProjectVersionPlugin
 */
class CoreProjectVersionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        if (target != target.rootProject) {
            throw GradleException("Plugin `dev.ellectronchik.versioning.config` must be applied only to the root project")
        }

        target.extensions.create(
            PluginProps.EXTENSION_NAME,
            VersioningExtension::class.java,
        )
    }
}
