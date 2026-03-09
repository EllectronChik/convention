package convention.publishing

import com.google.common.truth.Truth.assertThat
import convention.BasePluginTest
import org.junit.jupiter.api.Test
import java.io.File

class ModulePublishingPluginTest : BasePluginTest() {
    @Test
    fun `CorePublishingPlugin fails when applied to a subproject`() {
        withSubproject(
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
                kotlin("jvm")
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
                kotlin("jvm")
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
        val kotlinAndroidPlugin = if (!isAgp9OrNewer) "kotlin(\"android\")" else ""

        buildFile.writeText(
            """
            plugins {
                id ("com.android.library")
                $kotlinAndroidPlugin
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

    @Test
    fun `kotlin JVM publishing registers Dokka Javadoc tasks when configured`() {
        buildFile.writeText(
            """
            plugins {
                kotlin("jvm")
                id("dev.ellectronchik.publishing.config")
                id("dev.ellectronchik.publishing")
            }
            
            group = "dev.test"
            version = "1.0.0"
            
            corePublishing {
                withJavadocJar = true
                useDokka = true
            }
            """.trimIndent(),
        )

        val result = runGradle(TASKS_ARG)
        assertThat(result.output).contains("dokkaJavadocJar")
    }

    @Test
    fun `Android Library publishing registers Dokka Javadoc tasks when configured`() {
        val kotlinAndroidPlugin = if (!isAgp9OrNewer) "kotlin(\"android\")" else ""

        buildFile.writeText(
            """
            plugins {
                id ("com.android.library")
                $kotlinAndroidPlugin
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
                withJavadocJar = true
                useDokka = true
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

        assertThat(result.output).contains("dokkaJavadocJar")
    }
}
