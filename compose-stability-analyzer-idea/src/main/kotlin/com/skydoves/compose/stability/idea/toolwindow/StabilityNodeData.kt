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
package com.skydoves.compose.stability.idea.toolwindow

/**
 * Sealed class representing different types of nodes in the stability tree.
 */
public sealed class StabilityNodeData {

  /**
   * Module node (root level grouping)
   */
  public data class Module(
    val name: String,
    val skippableCount: Int,
    val unskippableCount: Int,
  ) : StabilityNodeData()

  /**
   * Package node (second level grouping)
   */
  public data class Package(
    val name: String,
    val skippableCount: Int,
    val unskippableCount: Int,
  ) : StabilityNodeData()

  /**
   * File node (third level grouping)
   */
  public data class File(
    val name: String,
    val skippableCount: Int,
    val unskippableCount: Int,
  ) : StabilityNodeData()

  /**
   * Individual composable function node (leaf)
   */
  public data class Composable(
    val info: ComposableInfo,
  ) : StabilityNodeData()

  /**
   * Empty state message node (shown when no composables found)
   */
  public data class EmptyMessage(
    val message: String,
  ) : StabilityNodeData()
}

/**
 * Information about a single composable function.
 */
public data class ComposableInfo(
  val functionName: String,
  val moduleName: String,
  val packageName: String,
  val fileName: String,
  val filePath: String,
  val line: Int,
  val isSkippable: Boolean,
  val isRestartable: Boolean,
  val isRuntime: Boolean,
  val parameters: List<ParameterInfo>,
)

/**
 * Information about a composable parameter.
 */
public data class ParameterInfo(
  val name: String,
  val type: String,
  val isStable: Boolean,
  val isRuntime: Boolean,
)

/**
 * Statistics about all composables in the project.
 */
public data class StabilityStats(
  val totalCount: Int = 0,
  val skippableCount: Int = 0,
  val unskippableCount: Int = 0,
)

/**
 * Results from scanning the project for composables.
 */
public data class ComposableStabilityResults(
  val composables: List<ComposableInfo>,
  val stats: StabilityStats,
)
