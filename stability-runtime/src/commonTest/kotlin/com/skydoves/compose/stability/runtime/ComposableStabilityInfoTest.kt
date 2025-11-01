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
import kotlin.test.assertTrue

class ComposableStabilityInfoTest {

  @Test
  fun testComposableStabilityInfo_allStableParameters() {
    val info = ComposableStabilityInfo(
      name = "UserCard",
      fqName = "com.example.UserCard",
      isRestartable = true,
      isSkippable = true,
      isReadonly = false,
      parameters = listOf(
        ParameterStabilityInfo("user", "User", ParameterStability.STABLE),
        ParameterStabilityInfo("onClick", "() -> Unit", ParameterStability.STABLE),
      ),
    )

    assertEquals("UserCard", info.name)
    assertEquals("com.example.UserCard", info.fqName)
    assertTrue(info.isRestartable)
    assertTrue(info.isSkippable)
    assertFalse(info.hasUnstableParameters())
    assertTrue(info.getUnstableParameters().isEmpty())
  }

  @Test
  fun testComposableStabilityInfo_withUnstableParameters() {
    val info = ComposableStabilityInfo(
      name = "MutableCard",
      fqName = "com.example.MutableCard",
      isRestartable = true,
      isSkippable = false,
      isReadonly = false,
      parameters = listOf(
        ParameterStabilityInfo("user", "User", ParameterStability.STABLE),
        ParameterStabilityInfo("data", "MutableData", ParameterStability.UNSTABLE),
        ParameterStabilityInfo("items", "MutableList<String>", ParameterStability.UNSTABLE),
      ),
    )

    assertTrue(info.hasUnstableParameters())
    assertEquals(2, info.getUnstableParameters().size)
    assertEquals(listOf("data", "items"), info.getUnstableParameters().map { it.name })
  }

  @Test
  fun testComposableStabilityInfo_noParameters() {
    val info = ComposableStabilityInfo(
      name = "EmptyCard",
      fqName = "com.example.EmptyCard",
      isRestartable = true,
      isSkippable = true,
      isReadonly = false,
      parameters = emptyList(),
    )

    assertFalse(info.hasUnstableParameters())
    assertTrue(info.getUnstableParameters().isEmpty())
  }

  @Test
  fun testComposableStabilityInfo_getSummary_skippable() {
    val info = ComposableStabilityInfo(
      name = "UserCard",
      fqName = "com.example.UserCard",
      isRestartable = true,
      isSkippable = true,
      isReadonly = false,
      parameters = listOf(
        ParameterStabilityInfo("user", "User", ParameterStability.STABLE),
      ),
    )

    val summary = info.getSummary()
    assertTrue(summary.contains("UserCard"))
    assertTrue(summary.contains("✅ Skippable"))
  }

  @Test
  fun testComposableStabilityInfo_getSummary_restartableNotSkippable() {
    val info = ComposableStabilityInfo(
      name = "UnstableCard",
      fqName = "com.example.UnstableCard",
      isRestartable = true,
      isSkippable = false,
      isReadonly = false,
      parameters = listOf(
        ParameterStabilityInfo("data", "MutableData", ParameterStability.UNSTABLE),
      ),
    )

    val summary = info.getSummary()
    assertTrue(summary.contains("UnstableCard"))
    assertTrue(summary.contains("⚠️ Restartable but not skippable"))
    assertTrue(summary.contains("unstable: data"))
  }

  @Test
  fun testComposableStabilityInfo_getSummary_notRestartable() {
    val info = ComposableStabilityInfo(
      name = "BrokenCard",
      fqName = "com.example.BrokenCard",
      isRestartable = false,
      isSkippable = false,
      isReadonly = false,
      parameters = emptyList(),
    )

    val summary = info.getSummary()
    assertTrue(summary.contains("BrokenCard"))
    assertTrue(summary.contains("❌ Not restartable"))
  }

  @Test
  fun testComposableStabilityInfo_getSummary_multipleUnstableParameters() {
    val info = ComposableStabilityInfo(
      name = "ComplexCard",
      fqName = "com.example.ComplexCard",
      isRestartable = true,
      isSkippable = false,
      isReadonly = false,
      parameters = listOf(
        ParameterStabilityInfo("user", "User", ParameterStability.UNSTABLE),
        ParameterStabilityInfo("data", "Data", ParameterStability.STABLE),
        ParameterStabilityInfo("items", "List", ParameterStability.UNSTABLE),
      ),
    )

    val summary = info.getSummary()
    assertTrue(summary.contains("ComplexCard"))
    assertTrue(summary.contains("unstable: user, items"))
  }

  @Test
  fun testComposableStabilityInfo_withReceivers() {
    val info = ComposableStabilityInfo(
      name = "ExtensionCard",
      fqName = "com.example.ExtensionCard",
      isRestartable = true,
      isSkippable = false,
      isReadonly = false,
      parameters = listOf(
        ParameterStabilityInfo("text", "String", ParameterStability.STABLE),
      ),
      receivers = listOf(
        ReceiverStabilityInfo(
          type = "ColumnScope",
          stability = ParameterStability.STABLE,
          reason = "Compose scope",
          receiverKind = ReceiverKind.EXTENSION,
        ),
      ),
    )

    assertEquals(1, info.receivers.size)
    assertEquals("ColumnScope", info.receivers[0].type)
    assertEquals(ReceiverKind.EXTENSION, info.receivers[0].receiverKind)
    assertFalse(info.hasUnstableReceivers())
  }

  @Test
  fun testComposableStabilityInfo_withUnstableReceivers() {
    val info = ComposableStabilityInfo(
      name = "UnstableExtension",
      fqName = "com.example.UnstableExtension",
      isRestartable = true,
      isSkippable = false,
      isReadonly = false,
      parameters = emptyList(),
      receivers = listOf(
        ReceiverStabilityInfo(
          type = "MutableScope",
          stability = ParameterStability.UNSTABLE,
          reason = "has mutable properties",
          receiverKind = ReceiverKind.EXTENSION,
        ),
      ),
    )

    assertTrue(info.hasUnstableReceivers())
    assertEquals(1, info.getUnstableReceivers().size)
    assertEquals("MutableScope", info.getUnstableReceivers()[0].type)
  }

  @Test
  fun testComposableStabilityInfo_strongSkippingMode() {
    val info = ComposableStabilityInfo(
      name = "StrongSkipCard",
      fqName = "com.example.StrongSkipCard",
      isRestartable = true,
      isSkippable = true,
      isReadonly = false,
      parameters = listOf(
        ParameterStabilityInfo("data", "MutableData", ParameterStability.UNSTABLE),
      ),
      isSkippableInStrongSkippingMode = true,
    )

    assertTrue(info.isSkippableInStrongSkippingMode)
    assertTrue(info.isSkippable)
    assertTrue(info.hasUnstableParameters())
  }

  @Test
  fun testComposableStabilityInfo_multipleReceiverKinds() {
    val info = ComposableStabilityInfo(
      name = "ComplexReceiver",
      fqName = "com.example.ComplexReceiver",
      isRestartable = true,
      isSkippable = true,
      isReadonly = false,
      parameters = emptyList(),
      receivers = listOf(
        ReceiverStabilityInfo(
          type = "ColumnScope",
          stability = ParameterStability.STABLE,
          receiverKind = ReceiverKind.EXTENSION,
        ),
        ReceiverStabilityInfo(
          type = "MyClass",
          stability = ParameterStability.STABLE,
          receiverKind = ReceiverKind.DISPATCH,
        ),
        ReceiverStabilityInfo(
          type = "MyContext",
          stability = ParameterStability.UNSTABLE,
          receiverKind = ReceiverKind.CONTEXT,
        ),
      ),
    )

    assertEquals(3, info.receivers.size)
    assertTrue(info.hasUnstableReceivers())
    assertEquals(1, info.getUnstableReceivers().size)
    assertEquals(ReceiverKind.CONTEXT, info.getUnstableReceivers()[0].receiverKind)
  }
}
