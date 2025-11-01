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
package com.skydoves.compose.stability.idea.quickfix

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.psi.psiUtil.getParentOfType

/**
 * Intention action to add @TraceRecomposition annotation to composable functions.
 *
 * This helps developers easily enable recomposition tracking for debugging performance issues.
 */
public class AddTraceRecompositionIntention :
  PsiElementBaseIntentionAction(),
  IntentionAction {

  override fun getFamilyName(): String = "Add @TraceRecomposition annotation"

  override fun getText(): String = "Add @TraceRecomposition to track recompositions"

  override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean {
    val function = element.getParentOfType<KtNamedFunction>(false) ?: return false

    if (!function.hasComposableAnnotation()) return false

    if (function.hasTraceRecompositionAnnotation()) return false

    return true
  }

  override fun invoke(project: Project, editor: Editor?, element: PsiElement) {
    val function = element.getParentOfType<KtNamedFunction>(false) ?: return

    // Add the annotation using PSI factory (K2 compatible)
    val factory = KtPsiFactory(project)
    val annotation = factory.createAnnotationEntry("@TraceRecomposition")

    val modifierList = function.modifierList
    if (modifierList != null) {
      modifierList.addBefore(annotation, modifierList.firstChild)
    } else {
      val newModifierList = factory.createModifierList("@TraceRecomposition")
      function.addBefore(newModifierList, function.funKeyword)
    }
  }

  private fun KtNamedFunction.hasComposableAnnotation(): Boolean {
    return annotationEntries.any {
      it.shortName?.asString() == "Composable"
    }
  }

  private fun KtNamedFunction.hasTraceRecompositionAnnotation(): Boolean {
    return annotationEntries.any {
      it.shortName?.asString() == "TraceRecomposition"
    }
  }
}
