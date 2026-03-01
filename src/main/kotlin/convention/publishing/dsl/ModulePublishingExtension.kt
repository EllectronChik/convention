package dev.ellectronchik.convention.publishing.dsl

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import javax.inject.Inject

abstract class ModulePublishingExtension
    @Inject
    constructor(
        objects: ObjectFactory,
    ) {
        val artifactId: Property<String> = objects.property(String::class.java)

        fun artifactId(value: String) {
            artifactId.set(value)
        }

        val overrideDefaults: OverrideDefaults = objects.newInstance(OverrideDefaults::class.java)

        fun overrideDefaults(action: OverrideDefaults.() -> Unit) = overrideDefaults.action()
    }
