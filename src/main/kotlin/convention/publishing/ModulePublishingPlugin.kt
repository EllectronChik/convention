package dev.ellectronchik.convention.publishing

import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.gradle.LibraryExtension
import dev.ellectronchik.convention.common.DependantIds
import dev.ellectronchik.convention.publishing.dsl.CorePublishingExtension
import dev.ellectronchik.convention.publishing.dsl.ModulePublishingExtension
import dev.ellectronchik.convention.publishing.internal.constants.PluginProps
import dev.ellectronchik.convention.publishing.models.PublicationType
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register

class ModulePublishingPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.rootProject.plugins.withId(PluginProps.CORE_PLUGIN_ID) {
            configureModulePublishing(target)
        }
    }

    private fun configureModulePublishing(target: Project) {
        target.pluginManager.apply("maven-publish")

        val publishing = target.extensions.getByType(PublishingExtension::class.java)

        val rootExtension = target.rootProject.extensions.getByType(CorePublishingExtension::class.java)

        rootExtension.repositoryConfig?.let { action ->
            publishing.repositories.action()
        }

        val moduleExtension = target.extensions.create("modulePublishing", ModulePublishingExtension::class.java)

        val artifactIdProvider = moduleExtension.artifactId.orElse(target.path.trimStart(':').replace(':', '-'))

        val groupIdProvider =
            moduleExtension.overrideDefaults.groupId
                .orElse(
                    rootExtension.groupId,
                ).orElse(target.provider { target.group.toString() })

        val versionProvider = target.provider { target.version.toString() }

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

        target.pluginManager.withPlugin(DependantIds.KOTLIN_JVM) {
            target.addPublication(
                publishing,
                "java",
                groupIdProvider,
                artifactIdProvider,
                versionProvider,
                useDokkaProvider,
                target.ensureDokkaJar(),
            )
        }

        target.pluginManager.withPlugin(DependantIds.ANDROID_LIBRARY) {
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
                target.ensureDokkaJar(),
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
        dokkaJavaDocJarProvider: Provider<Jar>,
    ) {
        val androidComponents = this.extensions.getByType(AndroidComponentsExtension::class.java)
        val androidExtension = this.extensions.getByType(LibraryExtension::class.java)

        androidComponents.onVariants { variant ->
            val typeToPublish =
                moduleExtension.overrideDefaults.publishVariant
                    .orElse(rootExtension.publishVariant)
                    .get()

            val shouldPublish =
                when (typeToPublish) {
                    PublicationType.DEBUG,
                    PublicationType.RELEASE,
                    -> variant.name == typeToPublish.id

                    PublicationType.ALL -> true
                }

            if (shouldPublish) {
                androidExtension.publishing {
                    singleVariant(variant.name) {
                        if (withSourceJarProvider.get()) withSourcesJar()
                        if (withJavadocJarProvider.get() && !useDokkaProvider.get()) withJavadocJar()
                    }
                }

                addPublication(
                    publishing = publishing,
                    name = variant.name,
                    groupIdProvider = groupIdProvider,
                    artifactIdProvider = artifactIdProvider,
                    versionProvider = versionProvider,
                    useDokkaProvider = useDokkaProvider,
                    dokkaJavadocJarProvider = dokkaJavaDocJarProvider,
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
        dokkaJavadocJarProvider: Provider<Jar>,
    ) {
        publishing.publications {
            val existing = findByName(name)
            if (existing == null) {
                register<MavenPublication>(name) {
                    this.groupId = groupIdProvider.get()
                    this.artifactId = artifactIdProvider.get()
                    this.version = versionProvider.get()

                    if (useDokkaProvider.get()) {
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
