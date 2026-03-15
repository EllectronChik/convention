package dev.ellectronchik.convention.publishing.dsl

import dev.ellectronchik.convention.publishing.models.PublicationType
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import javax.inject.Inject

/**
 * Optional per-module overrides for values inherited from root `corePublishing`.
 */
abstract class OverrideDefaults
    @Inject
    constructor(
        objects: ObjectFactory,
    ) {
        /** Android variant(s) to publish for this module. */
        val publishVariant: Property<PublicationType> =
            objects.property(PublicationType::class.java)

        /** Sets [publishVariant] to [value]. */
        fun publishVariant(value: PublicationType) {
            publishVariant.set(value)
        }

        /** Group ID override for this module. */
        val groupId: Property<String> = objects.property(String::class.java)

        /** Sets [groupId] to [value]. */
        fun groupId(value: String) {
            groupId.set(value)
        }

        /** Enables `sources` JAR for this module when supported. */
        val withSourceJar: Property<Boolean> = objects.property(Boolean::class.java)

        /** Sets [withSourceJar] to [value]. */
        fun withSourceJar(value: Boolean) {
            withSourceJar.set(value)
        }

        /** Enables Javadoc JAR for this module when supported. */
        val withJavadocJar: Property<Boolean> = objects.property(Boolean::class.java)

        /** Sets [withJavadocJar] to [value]. */
        fun withJavadocJar(value: Boolean) {
            withJavadocJar.set(value)
        }

        /** Uses Dokka-generated Javadoc JAR for this module when enabled. */
        val useDokka: Property<Boolean> = objects.property(Boolean::class.java)

        /** Sets [useDokka] to [value]. */
        fun useDokka(value: Boolean) {
            useDokka.set(value)
        }
    }
