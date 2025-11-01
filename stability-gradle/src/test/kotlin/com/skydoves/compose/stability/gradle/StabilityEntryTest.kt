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
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class StabilityEntryTest {

  @Test
  fun testStabilityEntry_creation() {
    val entry = StabilityEntry(
      qualifiedName = "com.example.UserCard",
      simpleName = "UserCard",
      visibility = "public",
      parameters = listOf(
        ParameterInfo("user", "User", "STABLE", "marked @Immutable"),
      ),
      returnType = "kotlin.Unit",
      skippable = true,
      restartable = true,
    )

    assertEquals("com.example.UserCard", entry.qualifiedName)
    assertEquals("UserCard", entry.simpleName)
    assertEquals("public", entry.visibility)
    assertEquals(1, entry.parameters.size)
    assertEquals("kotlin.Unit", entry.returnType)
    assertTrue(entry.skippable)
    assertTrue(entry.restartable)
  }

  @Test
  fun testStabilityEntry_withMultipleParameters() {
    val entry = StabilityEntry(
      qualifiedName = "com.example.ComplexCard",
      simpleName = "ComplexCard",
      visibility = "internal",
      parameters = listOf(
        ParameterInfo("name", "String", "STABLE"),
        ParameterInfo("age", "Int", "STABLE"),
        ParameterInfo("data", "MutableData", "UNSTABLE", "has var properties"),
      ),
      returnType = "kotlin.Unit",
      skippable = false,
      restartable = true,
    )

    assertEquals(3, entry.parameters.size)
    assertEquals("internal", entry.visibility)
    assertFalse(entry.skippable)
  }

  @Test
  fun testStabilityEntry_noParameters() {
    val entry = StabilityEntry(
      qualifiedName = "com.example.EmptyCard",
      simpleName = "EmptyCard",
      visibility = "public",
      parameters = emptyList(),
      returnType = "kotlin.Unit",
      skippable = true,
      restartable = true,
    )

    assertTrue(entry.parameters.isEmpty())
    assertTrue(entry.skippable)
  }

  @Test
  fun testStabilityEntry_equality() {
    val entry1 = StabilityEntry(
      qualifiedName = "com.example.Test",
      simpleName = "Test",
      visibility = "public",
      parameters = emptyList(),
      returnType = "kotlin.Unit",
      skippable = true,
      restartable = true,
    )

    val entry2 = StabilityEntry(
      qualifiedName = "com.example.Test",
      simpleName = "Test",
      visibility = "public",
      parameters = emptyList(),
      returnType = "kotlin.Unit",
      skippable = true,
      restartable = true,
    )

    assertEquals(entry1, entry2)
  }

  @Test
  fun testStabilityEntry_inequality_byQualifiedName() {
    val entry1 = StabilityEntry(
      qualifiedName = "com.example.Test1",
      simpleName = "Test1",
      visibility = "public",
      parameters = emptyList(),
      returnType = "kotlin.Unit",
      skippable = true,
      restartable = true,
    )

    val entry2 = StabilityEntry(
      qualifiedName = "com.example.Test2",
      simpleName = "Test2",
      visibility = "public",
      parameters = emptyList(),
      returnType = "kotlin.Unit",
      skippable = true,
      restartable = true,
    )

    assertNotEquals(entry1, entry2)
  }

  @Test
  fun testStabilityEntry_inequality_bySkippability() {
    val entry1 = StabilityEntry(
      qualifiedName = "com.example.Test",
      simpleName = "Test",
      visibility = "public",
      parameters = emptyList(),
      returnType = "kotlin.Unit",
      skippable = true,
      restartable = true,
    )

    val entry2 = StabilityEntry(
      qualifiedName = "com.example.Test",
      simpleName = "Test",
      visibility = "public",
      parameters = emptyList(),
      returnType = "kotlin.Unit",
      skippable = false,
      restartable = true,
    )

    assertNotEquals(entry1, entry2)
  }

  @Test
  fun testParameterInfo_creation() {
    val param = ParameterInfo(
      name = "user",
      type = "User",
      stability = "STABLE",
      reason = "marked @Immutable",
    )

    assertEquals("user", param.name)
    assertEquals("User", param.type)
    assertEquals("STABLE", param.stability)
    assertEquals("marked @Immutable", param.reason)
  }

  @Test
  fun testParameterInfo_withoutReason() {
    val param = ParameterInfo(
      name = "count",
      type = "Int",
      stability = "STABLE",
    )

    assertEquals("count", param.name)
    assertEquals("Int", param.type)
    assertEquals("STABLE", param.stability)
    assertNull(param.reason)
  }

  @Test
  fun testParameterInfo_unstable() {
    val param = ParameterInfo(
      name = "data",
      type = "MutableData",
      stability = "UNSTABLE",
      reason = "has var properties",
    )

    assertEquals("UNSTABLE", param.stability)
    assertEquals("has var properties", param.reason)
  }

  @Test
  fun testParameterInfo_runtime() {
    val param = ParameterInfo(
      name = "value",
      type = "T",
      stability = "RUNTIME",
      reason = "generic type",
    )

    assertEquals("RUNTIME", param.stability)
  }

  @Test
  fun testParameterInfo_equality() {
    val param1 = ParameterInfo("test", "String", "STABLE", "reason")
    val param2 = ParameterInfo("test", "String", "STABLE", "reason")

    assertEquals(param1, param2)
  }

  @Test
  fun testParameterInfo_inequality() {
    val param1 = ParameterInfo("test", "String", "STABLE", "reason")
    val param2 = ParameterInfo("test", "String", "UNSTABLE", "reason")

    assertNotEquals(param1, param2)
  }

  @Test
  fun testStabilityEntry_copy() {
    val original = StabilityEntry(
      qualifiedName = "com.example.Test",
      simpleName = "Test",
      visibility = "public",
      parameters = emptyList(),
      returnType = "kotlin.Unit",
      skippable = true,
      restartable = true,
    )

    val copy = original.copy(skippable = false)

    assertEquals("com.example.Test", copy.qualifiedName)
    assertFalse(copy.skippable)
    assertTrue(copy.restartable)
  }

  @Test
  fun testParameterInfo_copy() {
    val original = ParameterInfo("test", "String", "STABLE", "reason")
    val copy = original.copy(stability = "UNSTABLE")

    assertEquals("test", copy.name)
    assertEquals("String", copy.type)
    assertEquals("UNSTABLE", copy.stability)
    assertEquals("reason", copy.reason)
  }
}
