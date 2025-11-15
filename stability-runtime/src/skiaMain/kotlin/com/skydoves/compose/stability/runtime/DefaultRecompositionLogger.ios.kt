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

/**
 * iOS implementation of DefaultRecompositionLogger that uses println.
 *
 * Example output:
 * ```
 * [Recomposition #3] UserProfile (tag: user-screen)
 *   ├─ user: User changed (User@abc123 → User@def456)
 *   ├─ count: Int stable (42)
 *   ├─ onClick: () -> Unit stable
 *   └─ Unstable parameters: [user]
 * ```
 */
public actual class DefaultRecompositionLogger : RecompositionLogger {
  public actual constructor()

  actual override fun log(event: RecompositionEvent) {
    val tagSuffix = if (event.tag.isNotEmpty()) " (tag: ${event.tag})" else ""

    println("[Recomposition #${event.recompositionCount}] ${event.composableName}$tagSuffix")

    // Log parameter changes
    event.parameterChanges.forEachIndexed { index, change ->
      val isLast = index == event.parameterChanges.size - 1
      val prefix = if (isLast) "  └─" else "  ├─"

      val status = when {
        change.changed -> {
          val oldStr = safeToString(change.oldValue)
          val newStr = safeToString(change.newValue)
          "changed ($oldStr → $newStr)"
        }

        change.stable -> "stable (${safeToString(change.newValue)})"
        else -> "unstable (${safeToString(change.newValue)})"
      }

      println("$prefix ${change.name}: ${change.type} $status")
    }

    // Log unstable parameters summary
    if (event.unstableParameters.isNotEmpty()) {
      println("  └─ Unstable parameters: ${event.unstableParameters}")
    }
  }

  /**
   * Safely converts a value to string, handling function types and reflection errors.
   * Falls back to a simple representation if toString() throws an exception.
   */
  private fun safeToString(value: Any?): String {
    if (value == null) return "null"

    return try {
      value.toString()
    } catch (e: Throwable) {
      // Fallback for any toString() failures (including reflection errors)
      // On iOS/native, we don't have javaClass.simpleName, so use a simpler approach
      "Object@${value.hashCode().toString(16)}"
    }
  }
}
