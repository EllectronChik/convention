package dev.ellectronchik.convention.publishing.internal.models

import org.gradle.api.provider.Provider

internal class CommonProviders(
    val groupIdProvider: Provider<String>,
    val artifactIdProvider: Provider<String>,
    val versionProvider: Provider<String>,
    val useDokkaProvider: Provider<Boolean>,
    val withSourceJarProvider: Provider<Boolean>,
    val withJavadocJarProvider: Provider<Boolean>,
)
