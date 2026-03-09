package dev.ellectronchik.convention.publishing

import com.android.build.api.dsl.LibraryExtension
import com.android.build.api.variant.LibraryAndroidComponentsExtension
import dev.ellectronchik.convention.common.DependentIds
import dev.ellectronchik.convention.publishing.dsl.CorePublishingExtension
import dev.ellectronchik.convention.publishing.dsl.ModulePublishingExtension
import dev.ellectronchik.convention.publishing.internal.constants.PluginProps
import dev.ellectronchik.convention.publishing.models.PublicationType
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.provider.Provider
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register

/**
 * Module-level publishing plugin.
 *
 * Requires [PluginProps.CORE_PLUGIN_ID] to be applied on the root project and then configures
 * `maven-publish` for Kotlin JVM and Android library modules.
 */
@Suppress("unused")
class ModulePublishingPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        if (!target.rootProject.pluginManager.hasPlugin(PluginProps.CORE_PLUGIN_ID)) {
            throw GradleException("Plugin ${PluginProps.CORE_PLUGIN_ID} should be applied to root project and configured")
        }
        target.rootProject.plugins.withId(PluginProps.CORE_PLUGIN_ID) {
            configureModulePublishing(target)
        }
    }

    private fun Project.checkStr(
        propertyName: String = "resource",
        moduleStringProvider: () -> String,
        rootStringProvider: () -> String,
    ): Provider<String> =
        this.provider {
            fun String.isUnspecified(): Boolean = (this.isBlank() || this == "unspecified")

            val moduleStr = moduleStringProvider()
            if (!moduleStr.isUnspecified()) return@provider moduleStr

            val rootStr = rootStringProvider()
            if (!rootStr.isUnspecified()) return@provider rootStr

            throw GradleException("No $propertyName was provided for module $name.")
        }

    private fun configureModulePublishing(target: Project) {
        target.pluginManager.apply("maven-publish")

        val publishing = target.extensions.getByType(PublishingExtension::class.java)

        val rootExtension = target.rootProject.extensions.getByType(CorePublishingExtension::class.java)

        rootExtension.repositoryConfig?.let { action ->
            publishing.repositories.action()
        }

        val moduleExtension = target.extensions.create("modulePublishing", ModulePublishingExtension::class.java)

        val artifactIdProvider =
            moduleExtension.artifactId.orElse(target.path.trimStart(':').replace(':', '-'))

        val groupIdProvider =
            moduleExtension.overrideDefaults.groupId
                .orElse(rootExtension.groupId)
                .orElse(
                    target.checkStr(
                        "group",
                        { target.group.toString() },
                        { target.rootProject.group.toString() },
                    ),
                )

        val versionProvider =
            target.checkStr(
                "version",
                { target.version.toString() },
                { target.rootProject.version.toString() },
            )

        val withSourceJarProvider =
            moduleExtension.overrideDefaults.withSourceJar
                .orElse(rootExtension.withSourceJar)
                .orElse(target.provider { true })

        val withJavadocJarProvider =
            moduleExtension.overrideDefaults.withJavadocJar
                .orElse(rootExtension.withJavadocJar)
                .orElse(target.provider { false })

        val useDokkaProvider =
            moduleExtension.overrideDefaults.useDokka
                .orElse(rootExtension.useDokka)
                .orElse(target.provider { true })

        target.pluginManager.withPlugin(DependentIds.KOTLIN_JVM) {
            val javaComponentName = "java"

            val sourceJarProvider =
                target.tasks.register<Jar>("sourcesJar") {
                    archiveClassifier.set("sources")
                    val javaExtension = target.extensions.getByType<JavaPluginExtension>()
                    from(javaExtension.sourceSets.getByName("main").allSource)
                }

            val javadocJarProvider =
                target.tasks.register<Jar>("javadocJar") {
                    archiveClassifier.set("javadoc")
                    val javadocTask = target.tasks.named("javadoc")
                    from(javadocTask)
                    dependsOn(javadocTask)
                }

            target.afterEvaluate {
                target.addPublication(
                    publishing = publishing,
                    publicationName = javaComponentName,
                    componentName = javaComponentName,
                    groupIdProvider = groupIdProvider,
                    artifactIdProvider = artifactIdProvider,
                    versionProvider = versionProvider,
                    useDokkaProvider = useDokkaProvider,
                    withJavadocJarProvider = withJavadocJarProvider,
                ) {
                    if (withSourceJarProvider.get()) {
                        artifact(sourceJarProvider)
                    }

                    if (withJavadocJarProvider.get() && !useDokkaProvider.get()) {
                        artifact(javadocJarProvider)
                    }
                }
            }
        }

        target.pluginManager.withPlugin(DependentIds.ANDROID_LIBRARY) {
            target.publishAndroid(
                rootExtension = rootExtension,
                moduleExtension = moduleExtension,
                publishing = publishing,
                groupIdProvider = groupIdProvider,
                artifactIdProvider = artifactIdProvider,
                versionProvider = versionProvider,
                withSourceJarProvider = withSourceJarProvider,
                withJavadocJarProvider = withJavadocJarProvider,
                useDokkaProvider = useDokkaProvider,
            )
        }
    }

    private fun Project.ensureDokkaJar(): TaskProvider<Jar> {
        pluginManager.apply("org.jetbrains.dokka")

        val existingTask = tasks.findByName("dokkaJavadocJar")

        return if (existingTask != null) {
            tasks.named<Jar>("dokkaJavadocJar")
        } else {
            tasks.register<Jar>("dokkaJavadocJar") {
                archiveClassifier.set("javadoc")
                from(tasks.named("dokkaJavadoc"))
            }
        }
    }

    private fun Project.publishAndroid(
        rootExtension: CorePublishingExtension,
        moduleExtension: ModulePublishingExtension,
        publishing: PublishingExtension,
        groupIdProvider: Provider<String>,
        artifactIdProvider: Provider<String>,
        versionProvider: Provider<String>,
        withSourceJarProvider: Provider<Boolean>,
        withJavadocJarProvider: Provider<Boolean>,
        useDokkaProvider: Provider<Boolean>,
    ) {
        val androidComponents = this.extensions.getByType<LibraryAndroidComponentsExtension>()
        val androidExtension = this.extensions.getByType<LibraryExtension>()

        val publicationTypeProvider =
            moduleExtension.overrideDefaults.publishVariant.orElse(rootExtension.publishVariant)

        androidComponents.finalizeDsl {
            val shouldAttachJavadoc = withJavadocJarProvider.get() && !useDokkaProvider.get()
            val shouldAttachSources = withSourceJarProvider.get()

            when (
                val publicationType = publicationTypeProvider.get()
            ) {
                PublicationType.DEBUG, PublicationType.RELEASE -> {
                    androidExtension.publishing {
                        singleVariant(publicationType.id) {
                            if (shouldAttachSources) withSourcesJar()
                            if (shouldAttachJavadoc) withJavadocJar()
                        }
                    }
                }

                PublicationType.ALL -> {
                    androidExtension.publishing {
                        multipleVariants(publicationType.id) {
                            allVariants()
                            if (shouldAttachSources) withSourcesJar()
                            if (shouldAttachJavadoc) withJavadocJar()
                        }
                    }
                }
            }
        }

        afterEvaluate {
            val componentName =
                when (publicationTypeProvider.get()) {
                    PublicationType.DEBUG -> "debug"
                    PublicationType.RELEASE -> "release"
                    PublicationType.ALL -> "default"
                }

            addPublication(
                publishing = publishing,
                publicationName = componentName,
                componentName = componentName,
                groupIdProvider = groupIdProvider,
                artifactIdProvider = artifactIdProvider,
                versionProvider = versionProvider,
                useDokkaProvider = useDokkaProvider,
                withJavadocJarProvider = withJavadocJarProvider,
            )
        }
    }

    private fun Project.addPublication(
        publishing: PublishingExtension,
        publicationName: String,
        componentName: String = publicationName,
        groupIdProvider: Provider<String>,
        artifactIdProvider: Provider<String>,
        versionProvider: Provider<String>,
        useDokkaProvider: Provider<Boolean>,
        withJavadocJarProvider: Provider<Boolean>,
        alsoAttach: (MavenPublication.() -> Unit)? = null,
    ) {
        val shouldUseDokka = withJavadocJarProvider.get() && useDokkaProvider.get()
        val dokkaJavadocJarProvider = if (shouldUseDokka) this.ensureDokkaJar() else null

        val component =
            components.findByName(componentName)
                ?: throw GradleException(
                    "Expected software component '$componentName' was not created for project $path",
                )

        publishing.publications {
            if (findByName(publicationName) == null) {
                register<MavenPublication>(publicationName) {
                    groupId = groupIdProvider.get()
                    artifactId = artifactIdProvider.get()
                    version = versionProvider.get()
                    from(component)

                    alsoAttach?.invoke(this)

                    if (shouldUseDokka) {
                        artifact(dokkaJavadocJarProvider)
                    }
                }
            }
        }
    }
}
