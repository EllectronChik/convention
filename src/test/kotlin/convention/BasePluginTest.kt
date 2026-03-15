package convention

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.io.TempDir
import java.io.File

abstract class BasePluginTest {
    @TempDir
    lateinit var testProjectsDir: File

    protected lateinit var settingsFile: File
    protected lateinit var buildFile: File
    protected lateinit var gradlePropertiesFile: File

    @BeforeEach
    fun setup() {
        settingsFile = File(testProjectsDir, GRADLE_SETTINGS_FILE_NAME)
        buildFile = File(testProjectsDir, GRADLE_BUILD_FILE_NAME)
        gradlePropertiesFile = File(testProjectsDir, GRADLE_PROPERTIES_FILE_NAME)
        setupTestProject()
    }

    protected fun setupTestProject() {
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

        val kotlinAndroidPlugin =
            if (!isAgp9OrNewer) {
                "id(\"org.jetbrains.kotlin.android\") apply false"
            } else {
                "" // Not needed for AGP 9+
            }

        buildFile.writeText(
            """
            plugins {
                id("com.android.library") apply false
                id("org.jetbrains.kotlin.jvm") apply false
                $kotlinAndroidPlugin
            }
            """.trimIndent(),
        )

        gradlePropertiesFile.writeText(
            "org.gradle.jvmargs=-Xmx1g -XX:MaxMetaspaceSize=512m\n",
        )
    }

    protected fun runGradle(
        vararg args: String,
        gradleVersion: String = testGradleVersion,
        expectFailure: Boolean = false,
    ): BuildResult =
        GradleRunner
            .create()
            .withProjectDir(testProjectsDir)
            .withArguments(*args, "--stacktrace")
            .withPluginClasspath()
            .withGradleVersion(gradleVersion)
            .run {
                if (expectFailure) buildAndFail() else build()
            }

    protected fun withSubproject(subprojectBuildFileData: String): File {
        val include = "include(\"$SUB_PROJECT_NAME\")"
        if (!settingsFile.readText().contains(include)) {
            settingsFile.appendText("\n$include\n")
        }

        val subDir = File(testProjectsDir, SUB_PROJECT_NAME).apply { mkdirs() }
        File(subDir, GRADLE_BUILD_FILE_NAME).writeText(subprojectBuildFileData)
        return subDir
    }

    companion object {
        const val SUB_PROJECT_NAME = "subproject"

        const val TASKS_ARG = "tasks"

        const val GRADLE_BUILD_FILE_NAME = "build.gradle.kts"
        const val GRADLE_SETTINGS_FILE_NAME = "settings.gradle.kts"
        const val GRADLE_PROPERTIES_FILE_NAME = "gradle.properties"

        private const val DEFAULT_TEST_GRADLE_VERSION = "8.14.2"
    }

    private val testGradleVersion: String
        get() = System.getProperty("testGradleVersion", DEFAULT_TEST_GRADLE_VERSION)

    protected val currentTestAgpVersion: String
        get() = System.getProperty("testAgpVersion", "8.13.2")

    protected val isAgp9OrNewer: Boolean
        get() {
            val majorVersion = currentTestAgpVersion.substringBefore('.').toIntOrNull() ?: 8
            return majorVersion >= 9
        }
}
