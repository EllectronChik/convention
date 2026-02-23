package dev.ellectronchik.convention.versioning.internal.extensions

import dev.ellectronchik.convention.versioning.dsl.VersioningExtension
import dev.ellectronchik.convention.versioning.exceptions.MissingPropertyException
import dev.ellectronchik.convention.versioning.internal.constants.PluginProps
import org.gradle.api.Project

internal fun Project.applyTopLevelVersion(extension: VersioningExtension) {
    val originalVersion = project.version

    project.version =
        object : Any() {
            override fun toString(): String {
                val overrideModuleVersion = extension.overrideModuleVersion.get()
                val currentVersionName =
                    extension.currentVersionName.orNull ?: throw MissingPropertyException(PluginProps.CURRENT_VERSION_NAME_PROPERTY)

                return if (originalVersion != Project.DEFAULT_VERSION &&
                    !overrideModuleVersion
                ) {
                    originalVersion.toString()
                } else {
                    currentVersionName
                }
            }
        }
}
