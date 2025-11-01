/*
 * Designed and developed by 2025 skydoves (Jaewoong Eum)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.skydoves.compose.stability.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Gradle plugin for Compose Stability Analyzer.
 * Automatically configures the Kotlin compiler plugin for stability analysis.
 */
public class StabilityAnalyzerGradlePlugin : Plugin<Project> {

  public companion object {
    // This version should match the version in gradle.properties
    // Update this when bumping the library version
    private const val VERSION = "0.4.0"
  }

  override fun apply(project: Project) {
    val extension = project.extensions.create(
      "composeStabilityAnalyzer",
      StabilityAnalyzerExtension::class.java,
      project.layout,
    )

    // Register stability dump task
    val stabilityDumpTask = project.tasks.register(
      "stabilityDump",
      StabilityDumpTask::class.java,
    ) {
      stabilityInputFile.set(
        project.layout.buildDirectory.file("stability/stability-info.json"),
      )
      outputDir.set(extension.stabilityValidation.outputDir)
      ignoredPackages.set(extension.stabilityValidation.ignoredPackages)
      ignoredClasses.set(extension.stabilityValidation.ignoredClasses)
    }

    // Register stability check task
    val stabilityCheckTask = project.tasks.register(
      "stabilityCheck",
      StabilityCheckTask::class.java,
    ) {
      stabilityInputFile.set(
        project.layout.buildDirectory.file("stability/stability-info.json"),
      )
      stabilityDir.set(extension.stabilityValidation.outputDir)
      ignoredPackages.set(extension.stabilityValidation.ignoredPackages)
      ignoredClasses.set(extension.stabilityValidation.ignoredClasses)
    }

    // Make check task depend on stabilityCheck if enabled
    project.tasks.named("check") {
      dependsOn(stabilityCheckTask)
    }

    // Configure after project evaluation
    project.afterEvaluate {
      val ignoredProjects = extension.stabilityValidation.ignoredProjects.get()
      val projectName = project.name
      val isIgnored = ignoredProjects.contains(projectName)

      if (isIgnored) {
        return@afterEvaluate
      }

      configureKotlinCompilerPlugin(project, extension)
      configureTaskDependencies(project, stabilityDumpTask, stabilityCheckTask)
    }

    // Add runtime dependency - use project dependency if available, otherwise use Maven coordinates
    val rootProject = project.rootProject
    val runtimeProject = rootProject.findProject(":stability-runtime")
    val lintProject = rootProject.findProject(":stability-lint")

    if (runtimeProject != null) {
      project.dependencies.add("implementation", runtimeProject)
      if (lintProject != null) {
        project.dependencies.add("lintChecks", lintProject)
      }
    } else {
      val dep = "com.github.skydoves:compose-stability-runtime:$VERSION"
      project.dependencies.add("implementation", dep)
    }
  }

  private fun configureKotlinCompilerPlugin(
    project: Project,
    extension: StabilityAnalyzerExtension,
  ) {
    val kotlinExtension = project.extensions.findByType(KotlinJvmProjectExtension::class.java)
      ?: project.extensions.findByType(KotlinAndroidProjectExtension::class.java)
      ?: project.extensions.findByType(KotlinMultiplatformExtension::class.java)

    if (kotlinExtension == null) {
      project.logger.warn(
        "Kotlin plugin not found. Compose Stability Analyzer will not be configured.",
      )
      return
    }

    // Apply the compiler plugin - use project dependency if available, otherwise use Maven coordinates
    val rootProject = project.rootProject
    val compilerProject = rootProject.findProject(":stability-compiler")
    if (compilerProject != null) {
      project.dependencies.add("kotlinCompilerPluginClasspath", compilerProject)
    } else {
      project.dependencies.add(
        "kotlinCompilerPluginClasspath",
        "com.github.skydoves:compose-stability-compiler:$VERSION",
      )
    }

    val includeTests = extension.stabilityValidation.includeTests.get()

    // Configure compiler arguments
    when (kotlinExtension) {
      is KotlinJvmProjectExtension -> {
        kotlinExtension.target.compilations.configureEach {
          if (includeTests || !isTestCompilation(name)) {
            compileTaskProvider.configure {
              compilerOptions.freeCompilerArgs.addAll(
                buildCompilerArguments(project, extension),
              )
            }
          }
        }
      }

      is KotlinAndroidProjectExtension -> {
        kotlinExtension.target.compilations.configureEach {
          if (includeTests || !isTestCompilation(name)) {
            compileTaskProvider.configure {
              compilerOptions.freeCompilerArgs.addAll(
                buildCompilerArguments(project, extension),
              )
            }
          }
        }
      }

      is KotlinMultiplatformExtension -> {
        kotlinExtension.targets.configureEach {
          compilations.configureEach {
            if (includeTests || !isTestCompilation(name)) {
              compileTaskProvider.configure {
                compilerOptions.freeCompilerArgs.addAll(
                  buildCompilerArguments(project, extension),
                )
              }
            }
          }
        }
      }
    }
  }

  /**
   * Check if a compilation is a test compilation based on its name.
   * Test compilations typically have names like "test", "testDebug", "testRelease",
   * "debugUnitTest", "debugAndroidTest", etc.
   */
  private fun isTestCompilation(compilationName: String): Boolean {
    val lowerName = compilationName.lowercase()
    return lowerName.contains("test") ||
      lowerName.contains("androidtest") ||
      lowerName.contains("unittest")
  }

  private fun buildCompilerArguments(
    project: Project,
    extension: StabilityAnalyzerExtension,
  ): List<String> {
    val pluginId = "com.skydoves.compose.stability.compiler"
    val args = mutableListOf<String>()

    args.add("-P")
    args.add("plugin:$pluginId:enabled=${extension.enabled.get()}")

    val stabilityOutputDir =
      project.layout.buildDirectory.dir("stability").get().asFile.absolutePath
    args.add("-P")
    args.add("plugin:$pluginId:stabilityOutputDir=$stabilityOutputDir")

    return args
  }

  private fun configureTaskDependencies(
    project: Project,
    stabilityDumpTask: org.gradle.api.tasks.TaskProvider<StabilityDumpTask>,
    stabilityCheckTask: org.gradle.api.tasks.TaskProvider<StabilityCheckTask>,
  ) {
    val extension = project.extensions.getByType(StabilityAnalyzerExtension::class.java)
    val includeTests = extension.stabilityValidation.includeTests.get()

    project.tasks.matching { task ->
      val isKotlinCompile = task.name.startsWith("compile") && task.name.contains("Kotlin")
      val isTestTask = isTestCompilation(task.name)

      // Include task if it's a Kotlin compile task and either:
      // 1. includeTests is true, OR
      // 2. it's not a test task
      isKotlinCompile && (includeTests || !isTestTask)
    }.all {
      stabilityDumpTask.get().dependsOn(this)
      stabilityCheckTask.get().dependsOn(this)
    }
  }
}
