package dev.ellectronchik.convention.publishing.internal.extensions

import com.android.build.api.dsl.LibraryExtension
import com.android.build.api.variant.LibraryAndroidComponentsExtension
import dev.ellectronchik.convention.publishing.dsl.CorePublishingExtension
import dev.ellectronchik.convention.publishing.dsl.ModulePublishingExtension
import dev.ellectronchik.convention.publishing.internal.models.CommonProviders
import dev.ellectronchik.convention.publishing.models.PublicationType
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.kotlin.dsl.getByType

internal fun Project.publishAndroid(
    rootExtension: CorePublishingExtension,
    moduleExtension: ModulePublishingExtension,
    publishing: PublishingExtension,
    providers: CommonProviders,
) {
    val androidComponents = this.extensions.getByType<LibraryAndroidComponentsExtension>()
    val androidExtension = this.extensions.getByType<LibraryExtension>()

    val publicationTypeProvider =
        moduleExtension.overrideDefaults.publishVariant.orElse(rootExtension.publishVariant)

    androidComponents.finalizeDsl {
        val shouldAttachJavadoc = providers.withJavadocJarProvider.get() && !providers.useDokkaProvider.get()
        val shouldAttachSources = providers.withSourceJarProvider.get()

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
            providers = providers,
        )
    }
}
