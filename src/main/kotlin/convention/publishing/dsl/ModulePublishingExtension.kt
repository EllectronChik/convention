package dev.ellectronchik.convention.publishing.dsl

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import javax.inject.Inject

/**
 * Per-module publishing configuration.
 *
 * Values in [overrideDefaults] take precedence over root `corePublishing` values.
 */
abstract class ModulePublishingExtension
    @Inject
    constructor(
        objects: ObjectFactory,
    ) {
        /** Artifact ID for this module publication. Defaults to the Gradle path (`:` replaced with `-`). */
        val artifactId: Property<String> = objects.property(String::class.java)

        /** Sets [artifactId] to [value]. */
        fun artifactId(value: String) {
            artifactId.set(value)
        }

        /** Overrides selected values inherited from `corePublishing`. */
        val overrideDefaults: OverrideDefaults = objects.newInstance(OverrideDefaults::class.java)

        /** Configures [overrideDefaults] using [action]. */
        fun overrideDefaults(action: OverrideDefaults.() -> Unit) = overrideDefaults.action()
    }
