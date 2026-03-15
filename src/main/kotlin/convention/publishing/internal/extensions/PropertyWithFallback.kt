package dev.ellectronchik.convention.publishing.internal.extensions

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.provider.Provider

internal fun Project.propertyWithFallback(
    propertyName: String = "resource",
    moduleStringProvider: () -> String,
    rootStringProvider: () -> String,
): Provider<String> =
    this.provider {
        fun String.isUnspecified(): Boolean = (this.isBlank() || this == "unspecified")

        val moduleStr = moduleStringProvider()
        if (!moduleStr.isUnspecified()) return@provider moduleStr

        val rootStr = rootStringProvider()
        if (!rootStr.isUnspecified()) return@provider rootStr

        throw GradleException("No $propertyName was provided for module $name.")
    }
