package dev.ellectronchik.convention.versioning.exceptions

import dev.ellectronchik.convention.versioning.internal.constants.PluginProps
import org.gradle.api.GradleException

/** Thrown when a required `versioning { ... }` property is missing on the root project. */
class MissingPropertyException(
    propertyName: String,
    details: String = "",
) : GradleException(
        "Missing `${PluginProps.EXTENSION_NAME}.$propertyName` in root build.gradle.kts.$details",
    )
