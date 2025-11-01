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
 * Tests for string utility functions used in stability analysis.
 */
class StringUtilsTest {

  @Test
  fun testPackageFiltering_exactMatch() {
    val qualifiedName = "com.example.test.UserCard"
    val packageName = qualifiedName.substringBeforeLast('.', "")

    assertTrue(packageName.startsWith("com.example"))
    assertTrue(packageName.startsWith("com.example.test"))
    assertFalse(packageName.startsWith("com.other"))
  }

  @Test
  fun testPackageFiltering_withWildcard() {
    val entries = listOf(
      "com.example.ui.UserCard",
      "com.example.ui.ProductCard",
      "com.other.DataCard",
    )

    val ignoredPackages = listOf("com.example.ui")

    val filtered = entries.filter { name ->
      val packageName = name.substringBeforeLast('.', "")
      !ignoredPackages.any { packageName.startsWith(it) }
    }

    assertEquals(1, filtered.size)
    assertEquals("com.other.DataCard", filtered[0])
  }

  @Test
  fun testClassNameExtraction() {
    val qualifiedName = "com.example.UserCard"
    val className = qualifiedName.substringAfterLast('.')

    assertEquals("UserCard", className)
  }

  @Test
  fun testClassFiltering() {
    val entries = listOf(
      "com.example.UserCard",
      "com.example.PreviewCard",
      "com.example.ProductCard",
    )

    val ignoredClasses = listOf("PreviewCard")

    val filtered = entries.filter { name ->
      val className = name.substringAfterLast('.')
      !ignoredClasses.contains(className)
    }

    assertEquals(2, filtered.size)
    assertFalse(filtered.contains("com.example.PreviewCard"))
  }

  @Test
  fun testJsonStringEscaping() {
    // Test that we handle escaped characters correctly
    val input = """contains "quotes" and \backslash"""
    val escaped = input
      .replace("\\", "\\\\")
      .replace("\"", "\\\"")

    assertTrue(escaped.contains("\\\""))
    assertTrue(escaped.contains("\\\\"))
  }

  @Test
  fun testJsonStringUnescaping() {
    val escaped = """contains \\\"quotes\\\" and \\\\backslash"""
    val unescaped = escaped
      .replace("\\\\", "\\")
      .replace("\\\"", "\"")
      .replace("\\n", "\n")
      .replace("\\r", "\r")
      .replace("\\t", "\t")

    assertTrue(unescaped.contains("\""))
    assertTrue(unescaped.contains("\\"))
  }

  @Test
  fun testRegexPatternMatching() {
    val json = """"qualifiedName": "com.example.UserCard""""

    val pattern = """"qualifiedName":\s*"([^"]+)"""".toRegex()
    val match = pattern.find(json)

    assertFalse(match == null)
    assertEquals("com.example.UserCard", match!!.groupValues[1])
  }

  @Test
  fun testBooleanExtraction() {
    val jsonTrue = """"skippable": true"""
    val jsonFalse = """"skippable": false"""

    val pattern = """"skippable":\s*(true|false)""".toRegex()

    val matchTrue = pattern.find(jsonTrue)
    assertEquals("true", matchTrue?.groupValues?.get(1))
    assertEquals(true, matchTrue?.groupValues?.get(1)?.toBoolean())

    val matchFalse = pattern.find(jsonFalse)
    assertEquals("false", matchFalse?.groupValues?.get(1))
    assertEquals(false, matchFalse?.groupValues?.get(1)?.toBoolean())
  }

  @Test
  fun testSimpleNameExtraction() {
    val qualifiedNames = listOf(
      "com.example.UserCard",
      "com.example.ui.ProductCard",
      "SimpleCard",
    )

    val simpleNames = qualifiedNames.map { it.substringAfterLast(".") }

    assertEquals(listOf("UserCard", "ProductCard", "SimpleCard"), simpleNames)
  }

  @Test
  fun testPackageNameExtraction() {
    val qualifiedName1 = "com.example.UserCard"
    val qualifiedName2 = "SimpleCard"

    val package1 = qualifiedName1.substringBeforeLast('.', "")
    val package2 = qualifiedName2.substringBeforeLast('.', "")

    assertEquals("com.example", package1)
    assertEquals("", package2)
  }

  @Test
  fun testCombinedFiltering() {
    val entries = listOf(
      StabilityEntry(
        "com.example.ui.UserCard",
        "UserCard",
        "public",
        emptyList(),
        "Unit",
        true,
        true,
      ),
      StabilityEntry(
        "com.example.ui.PreviewCard",
        "PreviewCard",
        "public",
        emptyList(),
        "Unit",
        true,
        true,
      ),
      StabilityEntry(
        "com.example.data.DataCard",
        "DataCard",
        "public",
        emptyList(),
        "Unit",
        true,
        true,
      ),
      StabilityEntry("com.other.TestCard", "TestCard", "public", emptyList(), "Unit", true, true),
    )

    val ignoredPackages = listOf("com.example.ui")
    val ignoredClasses = listOf("TestCard")

    val filtered = entries.filter { entry ->
      val packageName = entry.qualifiedName.substringBeforeLast('.', "")
      val className = entry.qualifiedName.substringAfterLast('.')

      !ignoredPackages.any { packageName.startsWith(it) } &&
        !ignoredClasses.contains(className)
    }

    assertEquals(1, filtered.size)
    assertEquals("com.example.data.DataCard", filtered[0].qualifiedName)
  }
}
