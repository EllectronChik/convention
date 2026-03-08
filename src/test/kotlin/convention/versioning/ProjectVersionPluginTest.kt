package convention.versioning

import com.google.common.truth.Truth.assertThat
import convention.BasePluginTest
import dev.ellectronchik.convention.versioning.internal.constants.PluginProps
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Test

class ProjectVersionPluginTest : BasePluginTest() {
    val taskName = "printVersion"
    val taskRunArg = ":$SUB_PROJECT_NAME:$taskName"

    @Test
    fun `ProjectVersionPlugin fails when applied to a subproject`() {
        withSubproject(
            """
            plugins {
                id("dev.ellectronchik.versioning")
            }
            """.trimIndent(),
        )

        val result = runGradle(TASKS_ARG, expectFailure = true)

        assertThat(result.output).contains("This plugin could be applied only to root-level file")
    }

    @Test
    fun `ProjectVersionPlugin applies top-level version to kotlin jvm subproject`() {
        buildFile.writeText(
            """
            plugins {
                id("dev.ellectronchik.versioning")
            }
            
            versioning {
                currentVersionName = "1.2.3"
                overrideModuleVersion = true
            }
            """.trimIndent(),
        )

        val taskName = "printVersion"
        val taskRunArg = ":$SUB_PROJECT_NAME:$taskName"

        withSubproject(
            """
            plugins {
                kotlin("jvm") 
            }
            
            tasks.register("$taskName") {
                doLast {
                    println("SUBPROJECT_VERSION=" + project.version)
                }
            }
            """.trimIndent(),
        )

        val result = runGradle(taskRunArg)

        assertThat(result.task(taskRunArg)?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.output).contains("SUBPROJECT_VERSION=1.2.3")
    }

    @Test
    fun `ProjectVersionPlugin respects overrideModuleVersion when set to false`() {
        buildFile.writeText(
            """
            plugins {
                id("dev.ellectronchik.versioning")
            }
            
            versioning {
                currentVersionName.set("1.2.3")
                overrideModuleVersion.set(false)
            }
            """.trimIndent(),
        )

        withSubproject(
            """
            plugins {
                kotlin("jvm")
            }
            
            version = "9.9.9"
            
            tasks.register("$taskName") {
                doLast {
                    println("SUBPROJECT_VERSION=" + project.version)
                }
            }
            """.trimIndent(),
        )

        val result = runGradle(taskRunArg)

        assertThat(result.output).contains("SUBPROJECT_VERSION=9.9.9")
        assertThat(result.output).doesNotContain("1.2.3")
    }

    @Test
    fun `ProjectVersionPlugin throws MissingPropertyException when version name is not set on evaluation`() {
        buildFile.writeText(
            """
            plugins {
                id("dev.ellectronchik.versioning")
            }
            
            versioning {
                overrideModuleVersion.set(true)
            }
            """.trimIndent(),
        )

        withSubproject(
            """
            plugins {
                kotlin("jvm")
            }
            
            tasks.register("$taskName") {
                doLast {
                    println("SUBPROJECT_VERSION=" + project.version)
                }
            }
            """.trimIndent(),
        )

        val result = runGradle(taskRunArg, expectFailure = true)

        assertThat(result.output).contains("MissingPropertyException")
        assertThat(result.output).contains(PluginProps.CURRENT_VERSION_NAME_PROPERTY)
    }
}
