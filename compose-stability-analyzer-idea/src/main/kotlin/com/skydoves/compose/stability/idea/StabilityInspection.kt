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
package com.skydoves.compose.stability.idea

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import com.skydoves.compose.stability.idea.quickfix.AddSuppressAnnotationFix
import com.skydoves.compose.stability.idea.settings.StabilitySettingsState
import com.skydoves.compose.stability.runtime.ComposableStabilityInfo
import com.skydoves.compose.stability.runtime.ParameterStability
import com.skydoves.compose.stability.runtime.ParameterStabilityInfo
import com.skydoves.compose.stability.runtime.ReceiverStabilityInfo
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.namedFunctionVisitor

/**
 * Inspection that warns about unstable composable functions.
 */
public class StabilityInspection : LocalInspectionTool() {

  private val settings: StabilitySettingsState
    get() = StabilitySettingsState.getInstance()

  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
    return namedFunctionVisitor { function ->
      if (!settings.isStabilityCheckEnabled) {
        return@namedFunctionVisitor
      }

      if (!settings.showWarnings) {
        return@namedFunctionVisitor
      }

      if (!function.isComposable()) return@namedFunctionVisitor

      if (function.isPreview()) return@namedFunctionVisitor

      if (isSuppressed(function)) return@namedFunctionVisitor

      val analysis = try {
        StabilityAnalyzer.analyze(function)
      } catch (_: Exception) {
        return@namedFunctionVisitor
      }

      if (!analysis.isSkippable) {
        val unstableParams = analysis.parameters.filter {
          it.stability != ParameterStability.STABLE
        }
        val unstableReceivers = analysis.receivers.filter {
          it.stability != ParameterStability.STABLE
        }

        if (unstableParams.isNotEmpty() || unstableReceivers.isNotEmpty()) {
          val message = buildProblemMessage(analysis, unstableParams, unstableReceivers)
          val suppressName = getSuppressName()
          val quickFix = AddSuppressAnnotationFix(suppressName)

          function.nameIdentifier?.let { nameElement ->
            holder.registerProblem(
              nameElement,
              message,
              ProblemHighlightType.WEAK_WARNING,
              quickFix,
            )
          }
        }
      }
    }
  }

  private fun buildProblemMessage(
    analysis: ComposableStabilityInfo,
    unstableParams: List<ParameterStabilityInfo>,
    unstableReceivers: List<ReceiverStabilityInfo>,
  ): String {
    return buildString {
      append("Composable '${analysis.name}' is not skippable due to ")

      val parts = mutableListOf<String>()

      if (unstableParams.isNotEmpty()) {
        val count = unstableParams.size
        val paramText = if (count == 1) "parameter" else "parameters"
        parts.add("$count unstable $paramText (${unstableParams.joinToString(", ") { it.name }})")
      }

      if (unstableReceivers.isNotEmpty()) {
        val count = unstableReceivers.size
        val receiverText = if (count == 1) "receiver" else "receivers"
        parts.add(
          "$count unstable $receiverText (${
            unstableReceivers.joinToString(
              ", ",
            ) { "${it.receiverKind.name.lowercase()}: ${it.type}" }
          })",
        )
      }

      append(parts.joinToString(" and "))
    }
  }

  /**
   * Check if the function is already suppressed.
   */
  private fun isSuppressed(function: KtNamedFunction): Boolean {
    val suppressAnnotation = function.annotationEntries.find {
      it.shortName?.asString() == "Suppress"
    } ?: return false

    val valueArguments = suppressAnnotation.valueArgumentList?.arguments ?: emptyList()
    return valueArguments.any { arg ->
      val text = arg.getArgumentExpression()?.text?.trim('"') ?: ""
      text == AddSuppressAnnotationFix.NON_SKIPPABLE_COMPOSABLE ||
        text == AddSuppressAnnotationFix.PARAMS_COMPARED_BY_REF
    }
  }

  /**
   * Get the appropriate suppress name based on the current mode.
   */
  private fun getSuppressName(): String {
    return if (settings.isStrongSkippingEnabled) {
      AddSuppressAnnotationFix.PARAMS_COMPARED_BY_REF
    } else {
      AddSuppressAnnotationFix.NON_SKIPPABLE_COMPOSABLE
    }
  }

  override fun getDisplayName(): String =
    StabilityConstants.Inspections.UNSTABLE_COMPOSABLE_DISPLAY_NAME

  override fun getGroupDisplayName(): String =
    StabilityConstants.Groups.COMPOSE

  override fun getShortName(): String =
    StabilityConstants.Inspections.UNSTABLE_COMPOSABLE_SHORT_NAME

  override fun isEnabledByDefault(): Boolean = true
}
