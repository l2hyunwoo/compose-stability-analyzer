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
 * Gets or creates a RecompositionTracker instance for tracking.
 *
 * This function is called by compiler-generated code to get a tracker instance.
 * The tracker manages its own state across recompositions using a global map.
 *
 * @param composableName The name of the composable function being tracked
 * @param tag Custom tag from @TraceRecomposition annotation
 * @param threshold Only log after this many recompositions
 * @return A RecompositionTracker instance for this composable
 */
public fun rememberRecompositionTracker(
  composableName: String,
  tag: String,
  threshold: Int,
): RecompositionTracker {
  val key = "$composableName|$tag"
  return trackerCache.getOrPut(key) {
    createRecompositionTracker(composableName, tag, threshold)
  }
}

// Global cache to persist trackers across recompositions
private val trackerCache = mutableMapOf<String, RecompositionTracker>()
