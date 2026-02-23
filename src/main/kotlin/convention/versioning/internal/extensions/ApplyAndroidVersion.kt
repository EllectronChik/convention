package dev.ellectronchik.convention.versioning.internal.extensions

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.variant.AndroidComponentsExtension
import dev.ellectronchik.convention.versioning.dsl.VersioningExtension
import dev.ellectronchik.convention.versioning.exceptions.MissingPropertyException
import dev.ellectronchik.convention.versioning.internal.constants.PluginProps
import org.gradle.api.Project

internal fun Project.applyAndroidVersion(extension: VersioningExtension) {
    val androidComponents = project.extensions.findByType(AndroidComponentsExtension::class.java)

    androidComponents?.finalizeDsl { androidDsl ->
        val overrideModuleVersion = extension.overrideModuleVersion.get()
        val currentVersionName =
            extension.currentVersionName.orNull ?: throw MissingPropertyException(PluginProps.CURRENT_VERSION_NAME_PROPERTY)
        val currentAndroidVersionCode =
            extension.currentAndroidVersionCode.orNull ?: throw MissingPropertyException(PluginProps.CURRENT_ANDROID_VERSION_PROPERTY)

        (androidDsl as? ApplicationExtension)?.defaultConfig {
            if (overrideModuleVersion) {
                versionName = currentVersionName
                versionCode = currentAndroidVersionCode
            } else {
                if (versionCode == null) versionCode = currentAndroidVersionCode
                if (versionName == null) versionName = currentVersionName
            }
        }
    }
}
