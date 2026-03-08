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

    @BeforeEach
    fun setup() {
        settingsFile = File(testProjectsDir, GRADLE_SETTINGS_FILE_NAME)
        buildFile = File(testProjectsDir, GRADLE_BUILD_FILE_NAME)

        settingsFile.writeText(
            """
            rootProject.name = "test-project"
            """.trimIndent(),
        )
    }

    protected fun runGradle(
        vararg args: String,
        expectFailure: Boolean = false,
    ): BuildResult =
        GradleRunner
            .create()
            .withProjectDir(testProjectsDir)
            .withArguments(*args, "--stacktrace")
            .withPluginClasspath()
            .run {
                if (expectFailure) buildAndFail() else build()
            }

    protected fun withSubproject(subprojectBuildFileData: String): File {
        settingsFile.writeText(
            """
            include("$SUB_PROJECT_NAME")
            """.trimIndent(),
        )

        val subDir = File(testProjectsDir, SUB_PROJECT_NAME).apply { mkdirs() }
        File(subDir, GRADLE_BUILD_FILE_NAME).writeText(subprojectBuildFileData)
        return subDir
    }

    companion object {
        const val SUB_PROJECT_NAME = "subproject"

        const val TASKS_ARG = "tasks"

        const val GRADLE_BUILD_FILE_NAME = "build.gradle.kts"
        const val GRADLE_SETTINGS_FILE_NAME = "settings.gradle.kts"
    }
}
