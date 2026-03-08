package dev.ellectronchik.convention.publishing

import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.gradle.LibraryExtension
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
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register

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
        name: String = "resource",
        moduleStringProvider: () -> String,
        rootStringProvider: () -> String,
    ) = this.provider {
        var res = moduleStringProvider()

        fun resUnspecified() = (res == "" || res == "unspecified")
        if (resUnspecified()) res = rootStringProvider()
        if (resUnspecified()) throw GradleException("No $name was provided for module ${this.name}. ")
        res
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
                .orElse(
                    rootExtension.groupId,
                ).orElse(
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
                .orElse(
                    rootExtension.withSourceJar,
                ).orElse(
                    target.provider {
                        true
                    },
                )

        val withJavadocJarProvider =
            moduleExtension.overrideDefaults.withJavadocJar
                .orElse(
                    rootExtension.withJavadocJar,
                ).orElse(
                    target.provider {
                        false
                    },
                )

        val useDokkaProvider =
            moduleExtension.overrideDefaults.useDokka
                .orElse(rootExtension.useDokka)
                .orElse(
                    target.provider {
                        true
                    },
                )

        target.pluginManager.withPlugin(DependentIds.KOTLIN_JVM) {
            val javaExtension = target.extensions.getByType(JavaPluginExtension::class.java)

            if (withSourceJarProvider.get()) {
                javaExtension.withSourcesJar()
            }

            if (withJavadocJarProvider.get() && !useDokkaProvider.get()) {
                javaExtension.withJavadocJar()
            }

            target.afterEvaluate {
                target.addPublication(
                    publishing,
                    "java",
                    groupIdProvider,
                    artifactIdProvider,
                    versionProvider,
                    useDokkaProvider,
                    withJavadocJarProvider,
                )
            }
        }

        target.pluginManager.withPlugin(DependentIds.ANDROID_LIBRARY) {
            target.publishAndroid(
                rootExtension,
                moduleExtension,
                publishing,
                groupIdProvider,
                artifactIdProvider,
                versionProvider,
                withSourceJarProvider,
                withJavadocJarProvider,
                useDokkaProvider,
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
        val androidComponents = this.extensions.getByType(AndroidComponentsExtension::class.java)
        val androidExtension = this.extensions.getByType(LibraryExtension::class.java)

        androidComponents.finalizeDsl {
            val shouldAttachJavadoc = withJavadocJarProvider.get() && !useDokkaProvider.get()
            val shouldAttachSources = withSourceJarProvider.get()

            when (
                val publicationType =
                    moduleExtension.overrideDefaults.publishVariant
                        .orElse(rootExtension.publishVariant)
                        .get()
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

            androidComponents.onVariants { variant ->
                addPublication(
                    publishing = publishing,
                    name = variant.name,
                    groupIdProvider = groupIdProvider,
                    artifactIdProvider = artifactIdProvider,
                    versionProvider = versionProvider,
                    useDokkaProvider = useDokkaProvider,
                    withJavadocJarProvider = withJavadocJarProvider,
                )
            }
        }
    }

    private fun Project.addPublication(
        publishing: PublishingExtension,
        name: String,
        groupIdProvider: Provider<String>,
        artifactIdProvider: Provider<String>,
        versionProvider: Provider<String>,
        useDokkaProvider: Provider<Boolean>,
        withJavadocJarProvider: Provider<Boolean>,
    ) {
        val shouldUseDokka = withJavadocJarProvider.get() && useDokkaProvider.get()
        val dokkaJavadocJarProvider = if (shouldUseDokka) this.ensureDokkaJar() else null

        publishing.publications {
            val existing = findByName(name)
            if (existing == null) {
                register<MavenPublication>(name) {
                    this.groupId = groupIdProvider.get()
                    this.artifactId = artifactIdProvider.get()
                    this.version = versionProvider.get()

                    if (shouldUseDokka) {
                        artifact(dokkaJavadocJarProvider)
                    }
                }
            }
        }

        components.configureEach {
            if (this.name == name) {
                val component = this@configureEach
                publishing.publications.named<MavenPublication>(name).configure {
                    from(component)
                }
            }
        }
    }
}
