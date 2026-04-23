package dev.ellectronchik.convention.versioning.internal.extensions

import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import dev.ellectronchik.convention.versioning.dsl.VersioningExtension
import dev.ellectronchik.convention.versioning.exceptions.MissingPropertyException
import dev.ellectronchik.convention.versioning.internal.constants.PluginProps
import org.gradle.api.Project

internal fun Project.applyAndroidVersion(extension: VersioningExtension) {
    val androidComponents = this.extensions.getByType(ApplicationAndroidComponentsExtension::class.java)

    androidComponents.finalizeDsl { appExtension ->
        val overrideModuleVersion = extension.overrideModuleVersion.get()
        val currentVersionName =
            extension.currentVersionName.orNull
                ?: throw MissingPropertyException(PluginProps.CURRENT_VERSION_NAME_PROPERTY)
        val currentAndroidVersionCode =
            extension.currentAndroidVersionCode.orNull
                ?: throw MissingPropertyException(PluginProps.CURRENT_ANDROID_VERSION_PROPERTY)

        appExtension.defaultConfig {
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
