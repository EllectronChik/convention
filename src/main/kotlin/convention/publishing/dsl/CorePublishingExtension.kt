package dev.ellectronchik.convention.publishing.dsl

import dev.ellectronchik.convention.publishing.models.PublicationType
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import javax.inject.Inject

abstract class CorePublishingExtension
    @Inject
    constructor(
        objects: ObjectFactory,
    ) {
        internal var repositoryConfig: (RepositoryHandler.() -> Unit)? = null

        fun repositories(action: RepositoryHandler.() -> Unit) {
            this.repositoryConfig = action
        }

        val publishVariant: Property<PublicationType> =
            objects.property(PublicationType::class.java).convention(PublicationType.RELEASE)

        fun publishVariant(value: PublicationType) {
            publishVariant.set(value)
        }

        val groupId: Property<String> = objects.property(String::class.java)

        fun groupId(value: String) {
            groupId.set(value)
        }

        val withSourceJar: Property<Boolean> = objects.property(Boolean::class.java)

        fun withSourceJar(value: Boolean) {
            withSourceJar.set(value)
        }

        val withJavadocJar: Property<Boolean> = objects.property(Boolean::class.java)

        fun withJavadocJar(value: Boolean) {
            withJavadocJar.set(value)
        }

        val useDokka: Property<Boolean> = objects.property(Boolean::class.java)

        fun useDokka(value: Boolean) {
            useDokka.set(value)
        }
    }
