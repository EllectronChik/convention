package dev.ellectronchik.convention.publishing.internal.extensions

import dev.ellectronchik.convention.publishing.dsl.CorePublishingExtension
import dev.ellectronchik.convention.publishing.dsl.ModulePublishingExtension
import dev.ellectronchik.convention.publishing.internal.models.CommonProviders
import org.gradle.api.Project

internal fun Project.configureProviders(
    moduleExtension: ModulePublishingExtension,
    rootExtension: CorePublishingExtension,
): CommonProviders {
    val artifactIdFallback =
        this.path
            .trimStart(':')
            .replace(':', '-')
            .takeIf { it.isNotBlank() } ?: this.name

    val artifactIdProvider =
        moduleExtension.artifactId.orElse(artifactIdFallback)

    val groupIdProvider =
        moduleExtension.overrideDefaults.groupId
            .orElse(rootExtension.groupId)
            .orElse(
                this.propertyWithFallback(
                    "group",
                    { this.group.toString() },
                    { this.rootProject.group.toString() },
                ),
            )

    val versionProvider =
        this.propertyWithFallback(
            "version",
            { this.version.toString() },
            { this.rootProject.version.toString() },
        )

    val withSourceJarProvider =
        moduleExtension.overrideDefaults.withSourceJar
            .orElse(rootExtension.withSourceJar)
            .orElse(this.provider { true })

    val withJavadocJarProvider =
        moduleExtension.overrideDefaults.withJavadocJar
            .orElse(rootExtension.withJavadocJar)
            .orElse(this.provider { false })

    val useDokkaProvider =
        moduleExtension.overrideDefaults.useDokka
            .orElse(rootExtension.useDokka)
            .orElse(this.provider { true })

    return CommonProviders(
        groupIdProvider = groupIdProvider,
        artifactIdProvider = artifactIdProvider,
        versionProvider = versionProvider,
        useDokkaProvider = useDokkaProvider,
        withSourceJarProvider = withSourceJarProvider,
        withJavadocJarProvider = withJavadocJarProvider,
    )
}
