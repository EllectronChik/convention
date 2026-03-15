package dev.ellectronchik.convention.publishing

import dev.ellectronchik.convention.common.DependentIds
import dev.ellectronchik.convention.publishing.dsl.CorePublishingExtension
import dev.ellectronchik.convention.publishing.dsl.ModulePublishingExtension
import dev.ellectronchik.convention.publishing.internal.constants.PluginProps
import dev.ellectronchik.convention.publishing.internal.extensions.addPublication
import dev.ellectronchik.convention.publishing.internal.extensions.configureProviders
import dev.ellectronchik.convention.publishing.internal.extensions.publishAndroid
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register

/**
 * Per-module plugin that reads the root [dev.ellectronchik.convention.publishing.dsl.CorePublishingExtension]
 * and optional per-module overrides from [dev.ellectronchik.convention.publishing.dsl.ModulePublishingExtension],
 * and configures Maven publishing for the current subproject.
 *
 * Apply this plugin to every module that should be published:
 * ```kotlin
 * // module build.gradle.kts
 * plugins {
 *     id("dev.ellectronchik.publishing")
 * }
 * ```
 *
 * @throws org.gradle.api.GradleException if [CorePublishingPlugin] has not been applied to the root project.
 * @see dev.ellectronchik.convention.publishing.CorePublishingPlugin
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

    private fun configureModulePublishing(target: Project) {
        target.pluginManager.apply("maven-publish")

        val publishing = target.extensions.getByType(PublishingExtension::class.java)

        val rootExtension = target.rootProject.extensions.getByType(CorePublishingExtension::class.java)

        rootExtension.repositoryConfig?.let { action ->
            publishing.repositories.action()
        }

        val moduleExtension = target.extensions.create(PluginProps.MODULE_EXTENSION_NAME, ModulePublishingExtension::class.java)

        val commonProviders = target.configureProviders(moduleExtension, rootExtension)

        target.pluginManager.withPlugin(DependentIds.KOTLIN_JVM) {
            val javaComponentName = "java"

            val sourceJarProvider =
                target.tasks.register<Jar>("sourcesJar") {
                    group = "build"
                    description = "Assembles a jar archive containing the main sources."
                    archiveClassifier.set("sources")
                    val javaExtension = target.extensions.getByType<JavaPluginExtension>()
                    from(javaExtension.sourceSets.getByName("main").allSource)
                }

            val javadocJarProvider =
                target.tasks.register<Jar>("javadocJar") {
                    group = "documentation"
                    description = "Assembles a jar archive containing the Javadoc API documentation."
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
                    providers = commonProviders,
                ) {
                    if (commonProviders.withSourceJarProvider.get()) {
                        artifact(sourceJarProvider)
                    }

                    if (commonProviders.withJavadocJarProvider.get() && !commonProviders.useDokkaProvider.get()) {
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
                providers = commonProviders,
            )
        }
    }
}
