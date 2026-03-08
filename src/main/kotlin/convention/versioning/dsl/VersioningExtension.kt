package dev.ellectronchik.convention.versioning.dsl

import dev.ellectronchik.convention.versioning.internal.constants.Defaults
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import javax.inject.Inject

/**
 * Root-level version configuration consumed by subprojects through the versioning plugin.
 */
abstract class VersioningExtension
    @Inject
    constructor(
        objects: ObjectFactory,
    ) {
        /**
         * If `true`, force module versions to use [currentVersionName].
         * If `false`, modules keep their own explicit version when set.
         */
        val overrideModuleVersion: Property<Boolean> = objects.property(Boolean::class.java).convention(Defaults.OVERRIDE_MODULE_VERSION)

        /** Target semantic version name propagated to modules. */
        val currentVersionName: Property<String> = objects.property(String::class.java)

        /** Target Android `versionCode` used for application modules. */
        val currentAndroidVersionCode: Property<Int> = objects.property(Int::class.java)

        fun overrideModuleVersion(value: Boolean) {
            overrideModuleVersion.set(value)
        }

        fun currentVersionName(value: String) {
            currentVersionName.set(value)
        }

        fun currentAndroidVersionCode(value: Int) {
            currentAndroidVersionCode.set(value)
        }
    }
