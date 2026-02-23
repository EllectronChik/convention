package dev.ellectronchik.convention.versioning.exceptions

import dev.ellectronchik.convention.versioning.internal.constants.PluginProps
import org.gradle.api.GradleException

class MissingPropertyException(
    propertyName: String,
    details: String = "",
) : GradleException(
        "Missing `${PluginProps.EXTENSION_NAME}.$propertyName` in root build.gradle.kts.$details",
    )
