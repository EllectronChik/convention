package dev.ellectronchik.convention.versioning

import dev.ellectronchik.convention.versioning.dsl.VersioningExtension
import dev.ellectronchik.convention.versioning.internal.constants.DependantIds
import dev.ellectronchik.convention.versioning.internal.constants.PluginProps
import dev.ellectronchik.convention.versioning.internal.extensions.applyAndroidVersion
import dev.ellectronchik.convention.versioning.internal.extensions.applyTopLevelVersion
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project

class ProjectVersionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        if (target != target.rootProject) {
            throw GradleException("This plugin could be applied only to root-level file \"build.gradle.kts\"")
        }

        val extension = target.extensions.create(PluginProps.EXTENSION_NAME, VersioningExtension::class.java)

        target.subprojects.forEach { project ->
            project.plugins.withId(DependantIds.ANDROID_APP) {
                project.applyAndroidVersion(extension)
            }
            project.plugins.withId(DependantIds.ANDROID_LIBRARY) {
                project.applyTopLevelVersion(extension)
            }
            project.plugins.withId(DependantIds.KOTLIN_JVM) {
                project.applyTopLevelVersion(extension)
            }
        }
    }
}
