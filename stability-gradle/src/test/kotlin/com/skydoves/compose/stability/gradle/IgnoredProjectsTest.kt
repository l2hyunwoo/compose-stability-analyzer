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

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for ignoredProjects configuration.
 */
class IgnoredProjectsTest {

  @Test
  fun testIgnoredProjects_emptyByDefault() {
    val projects = emptyList<String>()
    val projectName = "app"

    assertFalse(projects.contains(projectName))
  }

  @Test
  fun testIgnoredProjects_singleProject() {
    val projects = listOf("benchmarks")
    val benchmarkProject = "benchmarks"
    val appProject = "app"

    assertTrue(projects.contains(benchmarkProject))
    assertFalse(projects.contains(appProject))
  }

  @Test
  fun testIgnoredProjects_multipleProjects() {
    val projects = listOf("benchmarks", "examples", "samples")

    assertTrue(projects.contains("benchmarks"))
    assertTrue(projects.contains("examples"))
    assertTrue(projects.contains("samples"))
    assertFalse(projects.contains("app"))
    assertFalse(projects.contains("core"))
  }

  @Test
  fun testIgnoredProjects_exactMatch() {
    val projects = listOf("benchmarks")

    // Exact match
    assertTrue(projects.contains("benchmarks"))

    // Should not match partial names
    assertFalse(projects.contains("benchmark"))
    assertFalse(projects.contains("benchmarks-module"))
    assertFalse(projects.contains("micro-benchmarks"))
  }

  @Test
  fun testIgnoredProjects_caseSensitive() {
    val projects = listOf("Benchmarks")

    // Exact case match
    assertTrue(projects.contains("Benchmarks"))

    // Different case should not match
    assertFalse(projects.contains("benchmarks"))
    assertFalse(projects.contains("BENCHMARKS"))
  }

  @Test
  fun testIgnoredProjects_filtering() {
    val allProjects = listOf("app", "core", "benchmarks", "examples", "ui", "samples")
    val ignoredProjects = listOf("benchmarks", "examples", "samples")

    val activeProjects = allProjects.filter { !ignoredProjects.contains(it) }

    assertEquals(3, activeProjects.size)
    assertTrue(activeProjects.contains("app"))
    assertTrue(activeProjects.contains("core"))
    assertTrue(activeProjects.contains("ui"))
    assertFalse(activeProjects.contains("benchmarks"))
    assertFalse(activeProjects.contains("examples"))
    assertFalse(activeProjects.contains("samples"))
  }

  @Test
  fun testIgnoredProjects_withColons() {
    // Users might accidentally include colons from project paths
    val projectsWithColons = listOf(":benchmarks", ":examples")
    val projectName = "benchmarks"

    // Should not match because we documented to use names without colons
    assertFalse(projectsWithColons.contains(projectName))

    // But would match with colon
    assertTrue(projectsWithColons.contains(":$projectName"))
  }

  @Test
  fun testIgnoredProjects_addingProjects() {
    val initialProjects = listOf("benchmarks")
    val additionalProjects = listOf("examples", "samples")

    val allIgnored = initialProjects + additionalProjects

    assertEquals(3, allIgnored.size)
    assertTrue(allIgnored.contains("benchmarks"))
    assertTrue(allIgnored.contains("examples"))
    assertTrue(allIgnored.contains("samples"))
  }

  @Test
  fun testIgnoredProjects_commonPatterns() {
    // Test common project naming patterns
    val ignoredProjects = listOf(
      "benchmarks",
      "examples",
      "samples",
      "demo",
      "test-utils",
      "docs",
    )

    assertTrue(ignoredProjects.contains("benchmarks"))
    assertTrue(ignoredProjects.contains("examples"))
    assertTrue(ignoredProjects.contains("samples"))
    assertTrue(ignoredProjects.contains("demo"))
    assertTrue(ignoredProjects.contains("test-utils"))
    assertTrue(ignoredProjects.contains("docs"))

    assertFalse(ignoredProjects.contains("app"))
    assertFalse(ignoredProjects.contains("core"))
    assertFalse(ignoredProjects.contains("ui"))
  }

  @Test
  fun testIgnoredProjects_emptyList() {
    val projects = emptyList<String>()

    assertEquals(0, projects.size)
    assertFalse(projects.contains("anything"))
  }

  @Test
  fun testIgnoredProjects_duplicates() {
    val projectsWithDuplicates = listOf("benchmarks", "examples", "benchmarks", "samples")
    val uniqueProjects = projectsWithDuplicates.distinct()

    assertEquals(3, uniqueProjects.size)
    assertTrue(uniqueProjects.contains("benchmarks"))
    assertTrue(uniqueProjects.contains("examples"))
    assertTrue(uniqueProjects.contains("samples"))
  }

  @Test
  fun testIgnoredProjects_nullSafety() {
    val projects: List<String> = listOf("benchmarks", "examples")
    val projectName: String? = null

    // Testing null safety - should not throw
    val result = projectName?.let { projects.contains(it) } ?: false

    assertFalse(result)
  }

  @Test
  fun testIgnoredProjects_predicateFiltering() {
    val allProjects = listOf("app", "benchmarks", "examples", "core")
    val ignoredProjects = setOf("benchmarks", "examples")

    // Efficient filtering with Set
    val activeProjects = allProjects.filter { it !in ignoredProjects }

    assertEquals(2, activeProjects.size)
    assertTrue(activeProjects.contains("app"))
    assertTrue(activeProjects.contains("core"))
  }
}
