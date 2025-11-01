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
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RecompositionEventTest {

  @Test
  fun testRecompositionEvent_creation() {
    val event = RecompositionEvent(
      composableName = "UserProfile",
      tag = "user-screen",
      recompositionCount = 5,
      parameterChanges = listOf(
        ParameterChange("user", "User", null, "User@123", false, true),
      ),
      unstableParameters = listOf("data"),
    )

    assertEquals("UserProfile", event.composableName)
    assertEquals("user-screen", event.tag)
    assertEquals(5, event.recompositionCount)
    assertEquals(1, event.parameterChanges.size)
    assertEquals(1, event.unstableParameters.size)
  }

  @Test
  fun testRecompositionEvent_emptyTag() {
    val event = RecompositionEvent(
      composableName = "SimpleCard",
      tag = "",
      recompositionCount = 1,
      parameterChanges = emptyList(),
      unstableParameters = emptyList(),
    )

    assertEquals("", event.tag)
  }

  @Test
  fun testRecompositionEvent_multipleParameterChanges() {
    val event = RecompositionEvent(
      composableName = "ComplexCard",
      tag = "card",
      recompositionCount = 3,
      parameterChanges = listOf(
        ParameterChange("name", "String", "John", "Jane", true, true),
        ParameterChange("age", "Int", 30, 31, true, true),
        ParameterChange("data", "Data", "d1", "d2", true, false),
      ),
      unstableParameters = listOf("data"),
    )

    assertEquals(3, event.parameterChanges.size)
    assertEquals(1, event.unstableParameters.size)
  }

  @Test
  fun testRecompositionEvent_equality() {
    val event1 = RecompositionEvent(
      composableName = "Test",
      tag = "tag",
      recompositionCount = 1,
      parameterChanges = emptyList(),
      unstableParameters = emptyList(),
    )

    val event2 = RecompositionEvent(
      composableName = "Test",
      tag = "tag",
      recompositionCount = 1,
      parameterChanges = emptyList(),
      unstableParameters = emptyList(),
    )

    assertEquals(event1, event2)
  }

  @Test
  fun testRecompositionEvent_inequality() {
    val event1 = RecompositionEvent(
      composableName = "Test1",
      tag = "tag",
      recompositionCount = 1,
      parameterChanges = emptyList(),
      unstableParameters = emptyList(),
    )

    val event2 = RecompositionEvent(
      composableName = "Test2",
      tag = "tag",
      recompositionCount = 1,
      parameterChanges = emptyList(),
      unstableParameters = emptyList(),
    )

    assertNotEquals(event1, event2)
  }

  @Test
  fun testParameterChange_creation() {
    val change = ParameterChange(
      name = "user",
      type = "User",
      oldValue = "User@123",
      newValue = "User@456",
      changed = true,
      stable = false,
    )

    assertEquals("user", change.name)
    assertEquals("User", change.type)
    assertEquals("User@123", change.oldValue)
    assertEquals("User@456", change.newValue)
    assertTrue(change.changed)
    assertFalse(change.stable)
  }

  @Test
  fun testParameterChange_nullOldValue() {
    val change = ParameterChange(
      name = "data",
      type = "String",
      oldValue = null,
      newValue = "value",
      changed = false,
      stable = true,
    )

    assertNull(change.oldValue)
    assertEquals("value", change.newValue)
    assertFalse(change.changed)
  }

  @Test
  fun testParameterChange_nullNewValue() {
    val change = ParameterChange(
      name = "data",
      type = "String?",
      oldValue = "value",
      newValue = null,
      changed = true,
      stable = true,
    )

    assertEquals("value", change.oldValue)
    assertNull(change.newValue)
    assertTrue(change.changed)
  }

  @Test
  fun testParameterChange_bothNull() {
    val change = ParameterChange(
      name = "optional",
      type = "String?",
      oldValue = null,
      newValue = null,
      changed = false,
      stable = true,
    )

    assertNull(change.oldValue)
    assertNull(change.newValue)
    assertFalse(change.changed)
  }

  @Test
  fun testParameterChange_primitiveTypes() {
    val intChange = ParameterChange(
      name = "count",
      type = "Int",
      oldValue = 1,
      newValue = 2,
      changed = true,
      stable = true,
    )

    assertEquals(1, intChange.oldValue)
    assertEquals(2, intChange.newValue)

    val boolChange = ParameterChange(
      name = "enabled",
      type = "Boolean",
      oldValue = false,
      newValue = true,
      changed = true,
      stable = true,
    )

    assertEquals(false, boolChange.oldValue)
    assertEquals(true, boolChange.newValue)
  }

  @Test
  fun testParameterChange_equality() {
    val change1 = ParameterChange(
      name = "test",
      type = "String",
      oldValue = "a",
      newValue = "b",
      changed = true,
      stable = true,
    )

    val change2 = ParameterChange(
      name = "test",
      type = "String",
      oldValue = "a",
      newValue = "b",
      changed = true,
      stable = true,
    )

    assertEquals(change1, change2)
  }

  @Test
  fun testParameterChange_inequality_byName() {
    val change1 = ParameterChange(
      name = "test1",
      type = "String",
      oldValue = "a",
      newValue = "b",
      changed = true,
      stable = true,
    )

    val change2 = ParameterChange(
      name = "test2",
      type = "String",
      oldValue = "a",
      newValue = "b",
      changed = true,
      stable = true,
    )

    assertNotEquals(change1, change2)
  }

  @Test
  fun testParameterChange_inequality_byStability() {
    val change1 = ParameterChange(
      name = "test",
      type = "String",
      oldValue = "a",
      newValue = "b",
      changed = true,
      stable = true,
    )

    val change2 = ParameterChange(
      name = "test",
      type = "String",
      oldValue = "a",
      newValue = "b",
      changed = true,
      stable = false,
    )

    assertNotEquals(change1, change2)
  }

  @Test
  fun testRecompositionEvent_copy() {
    val original = RecompositionEvent(
      composableName = "Test",
      tag = "original",
      recompositionCount = 1,
      parameterChanges = emptyList(),
      unstableParameters = emptyList(),
    )

    val copy = original.copy(tag = "modified")

    assertEquals("Test", copy.composableName)
    assertEquals("modified", copy.tag)
    assertEquals(1, copy.recompositionCount)
    assertNotEquals(original, copy)
  }

  @Test
  fun testParameterChange_copy() {
    val original = ParameterChange(
      name = "test",
      type = "String",
      oldValue = "a",
      newValue = "b",
      changed = true,
      stable = true,
    )

    val copy = original.copy(changed = false)

    assertEquals("test", copy.name)
    assertEquals("String", copy.type)
    assertEquals("a", copy.oldValue)
    assertEquals("b", copy.newValue)
    assertFalse(copy.changed)
    assertTrue(copy.stable)
  }
}
