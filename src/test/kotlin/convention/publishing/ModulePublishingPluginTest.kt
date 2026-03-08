package convention.publishing

import com.google.common.truth.Truth.assertThat
import convention.BasePluginTest
import org.junit.jupiter.api.Test
import java.io.File

class ModulePublishingPluginTest : BasePluginTest() {
    @Test
    fun `CorePublishingPlugin fails when applied to a subproject`() {
        settingsFile.writeText(
            """
            include("$SUB_PROJECT_NAME")
            """.trimIndent(),
        )

        val subDir = File(testProjectsDir, SUB_PROJECT_NAME).apply { mkdirs() }
        File(subDir, GRADLE_BUILD_FILE_NAME).writeText(
            """
            plugins {
                id("dev.ellectronchik.publishing.config")
            }
            """.trimIndent(),
        )

        val result = runGradle(TASKS_ARG, expectFailure = true)

        assertThat(result.output).contains("CorePublishingPlugin must be applied only to the root project")
    }

    @Test
    fun `ModulePublishingPlugin fails when core plugin is missing from root`() {
        buildFile.writeText(
            """
            plugins {
            id("dev.ellectronchik.publishing")
            }
            """.trimIndent(),
        )

        val result = runGradle(TASKS_ARG, expectFailure = true)

        assertThat(result.output).contains("Plugin dev.ellectronchik.publishing.config should be applied to root project")
    }

    @Test
    fun `ModulePublishPlugin successfully configures Kotlin JVM publishing`() {
        buildFile.writeText(
            """
            plugins {
                kotlin("jvm") version "2.0.0"
                id("dev.ellectronchik.publishing.config")
                id("dev.ellectronchik.publishing")
            }
            
            group = "dev.test"
            version = "1.0.0"
            """.trimIndent(),
        )

        val result = runGradle(TASKS_ARG)

        assertThat(result.output).contains("publishJavaPublicationToMavenLocal")
    }

    @Test
    fun `ModulePublishPlugin throws error if group and version are unspecified`() {
        buildFile.writeText(
            """
            plugins {
                kotlin("jvm") version "2.0.0"
                id("dev.ellectronchik.publishing.config")
                id("dev.ellectronchik.publishing")
            }
            """.trimIndent(),
        )

        val result = runGradle(TASKS_ARG, expectFailure = true)

        assertThat(result.output).contains("No group was provided for module")
    }

    @Test
    fun `Android Library publishing registers correct variant tasks`() {
        settingsFile.writeText(
            """
            pluginManagement {
                repositories {
                    google()
                    mavenCentral()
                    gradlePluginPortal()
                }
            }
            rootProject.name = "test-project"
            """.trimIndent(),
        )

        buildFile.writeText(
            """
            plugins {
                id ("com.android.library")
                kotlin("android") version "2.0.0"
                id("dev.ellectronchik.publishing.config")
                id("dev.ellectronchik.publishing")
            }
            
            group = "dev.test"
            version = "1.0.0"
            
            android {
                namespace = "dev.test.lib"
                compileSdk = 36
                defaultConfig {
                    minSdk = 24
                }
            }
            
            corePublishing {
                useDokka = false
            }
            """.trimIndent(),
        )
        File(testProjectsDir, "src/main").mkdirs()
        File(testProjectsDir, "src/main/AndroidManifest.xml").writeText(
            """
            <manifest package="dev.test.library"/>
            """.trimIndent(),
        )

        val result = runGradle(TASKS_ARG)

        assertThat(result.output).contains("publishReleasePublicationToMavenLocal")
    }
}
