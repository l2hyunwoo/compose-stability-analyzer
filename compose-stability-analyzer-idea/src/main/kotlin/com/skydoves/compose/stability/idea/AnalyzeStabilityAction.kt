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

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.psi.util.PsiTreeUtil
import com.skydoves.compose.stability.runtime.ComposableStabilityInfo
import com.skydoves.compose.stability.runtime.ParameterStability
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction

/**
 * Action to analyze all composable functions in the current file.
 */
public class AnalyzeStabilityAction : AnAction() {

  override fun actionPerformed(e: AnActionEvent) {
    val project = e.project ?: return
    val psiFile = e.getData(CommonDataKeys.PSI_FILE) as? KtFile ?: return

    val composables = PsiTreeUtil.findChildrenOfType(
      psiFile,
      KtNamedFunction::class.java,
    )
      .filter { it.isComposable() }

    if (composables.isEmpty()) {
      NotificationGroupManager.getInstance()
        .getNotificationGroup("Compose Stability Analyzer")
        .createNotification(
          "No Composable Functions Found",
          "No @Composable functions found in the current file",
          NotificationType.INFORMATION,
        )
        .notify(project)
      return
    }

    val results = composables.mapNotNull { function ->
      try {
        StabilityAnalyzer.analyze(function)
      } catch (e: Exception) {
        null
      }
    }

    val report = buildReport(results)
    NotificationGroupManager.getInstance()
      .getNotificationGroup("Compose Stability Analyzer")
      .createNotification(
        "Compose Stability Analysis Complete",
        report,
        NotificationType.INFORMATION,
      )
      .notify(project)
  }

  private fun buildReport(results: List<ComposableStabilityInfo>): String {
    val skippableCount = results.count { it.isSkippable }
    val totalCount = results.size

    return buildString {
      append("Analyzed $totalCount composable functions\n\n")
      append("✅ Skippable: $skippableCount\n")
      append("❌ Not Skippable: ${totalCount - skippableCount}\n\n")

      if (skippableCount < totalCount) {
        append("Not Skippable Functions:\n")
        results.filter { !it.isSkippable }.forEach { analysis ->
          val unstableCount =
            analysis.parameters.count { it.stability != ParameterStability.STABLE }
          append("  • ${analysis.name} ($unstableCount unstable parameter")
          if (unstableCount != 1) append("s")
          append(")\n")
        }
      }
    }
  }

  override fun update(e: AnActionEvent) {
    val psiFile = e.getData(CommonDataKeys.PSI_FILE)
    e.presentation.isEnabledAndVisible = psiFile is KtFile
  }
}
