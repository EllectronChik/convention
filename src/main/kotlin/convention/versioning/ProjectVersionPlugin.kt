package dev.ellectronchik.convention.versioning

import dev.ellectronchik.convention.common.DependentIds
import dev.ellectronchik.convention.versioning.dsl.VersioningExtension
import dev.ellectronchik.convention.versioning.internal.constants.PluginProps
import dev.ellectronchik.convention.versioning.internal.extensions.applyAndroidVersion
import dev.ellectronchik.convention.versioning.internal.extensions.applyTopLevelVersion
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType

/**
 * Per-module plugin that reads the root [dev.ellectronchik.convention.versioning.dsl.VersioningExtension]
 * and applies the shared version to the current subproject.
 *
 * Apply this plugin to every module that should inherit the shared version:
 * ```kotlin
 * // module build.gradle.kts
 * plugins {
 *     id("dev.ellectronchik.versioning")
 * }
 * ```
 *
 * The plugin detects the module type via its applied plugins and delegates accordingly:
 * - **Android application** (`com.android.application`): sets `versionName` and `versionCode` inside `defaultConfig`.
 * - **Android library** (`com.android.library`) and **Kotlin JVM** (`org.jetbrains.kotlin.jvm`): sets `project.version`.
 *
 * @throws org.gradle.api.GradleException if [CoreProjectVersionPlugin] has not been applied to the root project.
 * @see dev.ellectronchik.convention.versioning.CoreProjectVersionPlugin
 */
class ProjectVersionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        if (!target.rootProject.pluginManager.hasPlugin(PluginProps.CORE_PLUGIN_ID)) {
            throw GradleException(
                "Plugin ${PluginProps.CORE_PLUGIN_ID} should be applied to root project and configured",
            )
        }

        val extension = target.rootProject.extensions.getByType<VersioningExtension>()

        target.pluginManager.withPlugin(DependentIds.ANDROID_APP) {
            target.applyAndroidVersion(extension)
        }
        target.pluginManager.withPlugin(DependentIds.ANDROID_LIBRARY) {
            target.applyTopLevelVersion(extension)
        }
        target.pluginManager.withPlugin(DependentIds.KOTLIN_JVM) {
            target.applyTopLevelVersion(extension)
        }
    }
}
