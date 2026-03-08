package dev.ellectronchik.convention.publishing.dsl

import dev.ellectronchik.convention.publishing.models.PublicationType
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import javax.inject.Inject

/**
 * Shared publishing defaults configured once on the root project.
 *
 * Module-level publishing can override these values through `modulePublishing.overrideDefaults`.
 */
abstract class CorePublishingExtension
    @Inject
    constructor(
        objects: ObjectFactory,
    ) {
        internal var repositoryConfig: (RepositoryHandler.() -> Unit)? = null

        /** Configures repositories used by generated `publishing` blocks. */
        fun repositories(action: RepositoryHandler.() -> Unit) {
            this.repositoryConfig = action
        }

        /** Variant(s) to publish for Android libraries. Defaults to [PublicationType.RELEASE]. */
        val publishVariant: Property<PublicationType> =
            objects.property(PublicationType::class.java).convention(PublicationType.RELEASE)

        fun publishVariant(value: PublicationType) {
            publishVariant.set(value)
        }

        /** Group ID used for published artifacts when not overridden per module. */
        val groupId: Property<String> = objects.property(String::class.java)

        fun groupId(value: String) {
            groupId.set(value)
        }

        /** Enables `sources` JAR attachment when supported by the module type. */
        val withSourceJar: Property<Boolean> = objects.property(Boolean::class.java)

        fun withSourceJar(value: Boolean) {
            withSourceJar.set(value)
        }

        /** Enables Javadoc JAR attachment when supported by the module type. */
        val withJavadocJar: Property<Boolean> = objects.property(Boolean::class.java)

        fun withJavadocJar(value: Boolean) {
            withJavadocJar.set(value)
        }

        /** Uses Dokka-generated Javadoc JAR instead of the default Javadoc task when enabled. */
        val useDokka: Property<Boolean> = objects.property(Boolean::class.java)

        fun useDokka(value: Boolean) {
            useDokka.set(value)
        }
    }
