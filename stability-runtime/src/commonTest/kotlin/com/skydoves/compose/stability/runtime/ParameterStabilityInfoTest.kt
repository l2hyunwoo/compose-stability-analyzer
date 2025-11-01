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
package com.skydoves.compose.stability.runtime

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

class ParameterStabilityInfoTest {

  @Test
  fun testParameterStabilityInfo_creation() {
    val param = ParameterStabilityInfo(
      name = "user",
      type = "User",
      stability = ParameterStability.STABLE,
      reason = "marked @Immutable",
    )

    assertEquals("user", param.name)
    assertEquals("User", param.type)
    assertEquals(ParameterStability.STABLE, param.stability)
    assertEquals("marked @Immutable", param.reason)
  }

  @Test
  fun testParameterStabilityInfo_withoutReason() {
    val param = ParameterStabilityInfo(
      name = "count",
      type = "Int",
      stability = ParameterStability.STABLE,
    )

    assertEquals("count", param.name)
    assertEquals("Int", param.type)
    assertEquals(ParameterStability.STABLE, param.stability)
    assertNull(param.reason)
  }

  @Test
  fun testParameterStabilityInfo_unstable() {
    val param = ParameterStabilityInfo(
      name = "data",
      type = "MutableData",
      stability = ParameterStability.UNSTABLE,
      reason = "has mutable properties",
    )

    assertEquals("data", param.name)
    assertEquals("MutableData", param.type)
    assertEquals(ParameterStability.UNSTABLE, param.stability)
    assertEquals("has mutable properties", param.reason)
  }

  @Test
  fun testParameterStabilityInfo_runtime() {
    val param = ParameterStabilityInfo(
      name = "value",
      type = "T",
      stability = ParameterStability.RUNTIME,
      reason = "generic type parameter",
    )

    assertEquals("value", param.name)
    assertEquals("T", param.type)
    assertEquals(ParameterStability.RUNTIME, param.stability)
    assertEquals("generic type parameter", param.reason)
  }

  @Test
  fun testParameterStabilityInfo_equality() {
    val param1 = ParameterStabilityInfo(
      name = "user",
      type = "User",
      stability = ParameterStability.STABLE,
      reason = "test",
    )

    val param2 = ParameterStabilityInfo(
      name = "user",
      type = "User",
      stability = ParameterStability.STABLE,
      reason = "test",
    )

    assertEquals(param1, param2)
    assertEquals(param1.hashCode(), param2.hashCode())
  }

  @Test
  fun testParameterStabilityInfo_inequality() {
    val param1 = ParameterStabilityInfo(
      name = "user",
      type = "User",
      stability = ParameterStability.STABLE,
    )

    val param2 = ParameterStabilityInfo(
      name = "user",
      type = "User",
      stability = ParameterStability.UNSTABLE,
    )

    assertNotEquals(param1, param2)
  }

  @Test
  fun testParameterStabilityInfo_copy() {
    val original = ParameterStabilityInfo(
      name = "user",
      type = "User",
      stability = ParameterStability.STABLE,
      reason = "original",
    )

    val copy = original.copy(reason = "modified")

    assertEquals("user", copy.name)
    assertEquals("User", copy.type)
    assertEquals(ParameterStability.STABLE, copy.stability)
    assertEquals("modified", copy.reason)
    assertNotEquals(original, copy)
  }
}
