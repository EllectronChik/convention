package dev.ellectronchik.convention.versioning.dsl

import dev.ellectronchik.convention.versioning.internal.constants.Defaults
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import javax.inject.Inject

abstract class VersioningExtension
    @Inject
    constructor(
        objects: ObjectFactory,
    ) {
        val overrideModuleVersion: Property<Boolean> = objects.property(Boolean::class.java).convention(Defaults.OVERRIDE_MODULE_VERSION)

        val currentVersionName: Property<String> = objects.property(String::class.java)

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
