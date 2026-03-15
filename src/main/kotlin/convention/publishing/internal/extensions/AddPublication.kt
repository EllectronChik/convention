package dev.ellectronchik.convention.publishing.internal.extensions

import dev.ellectronchik.convention.publishing.internal.models.CommonProviders
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.register

internal fun Project.addPublication(
    publishing: PublishingExtension,
    publicationName: String,
    componentName: String = publicationName,
    providers: CommonProviders,
    alsoAttach: (MavenPublication.() -> Unit)? = null,
) {
    val shouldUseDokka = providers.withJavadocJarProvider.get() && providers.useDokkaProvider.get()
    val dokkaJavadocJarProvider = if (shouldUseDokka) this.ensureDokkaJar() else null

    val component =
        components.findByName(componentName)
            ?: throw GradleException(
                "Expected software component '$componentName' was not created for project $path",
            )

    publishing.publications {
        if (findByName(publicationName) == null) {
            register<MavenPublication>(publicationName) {
                groupId = providers.groupIdProvider.get()
                artifactId = providers.artifactIdProvider.get()
                version = providers.versionProvider.get()
                from(component)

                alsoAttach?.invoke(this)

                if (shouldUseDokka) {
                    artifact(dokkaJavadocJarProvider)
                }
            }
        }
    }
}
