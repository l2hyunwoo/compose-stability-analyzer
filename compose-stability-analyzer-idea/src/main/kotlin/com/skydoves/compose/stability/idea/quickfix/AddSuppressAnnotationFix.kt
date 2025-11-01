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

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtPsiFactory

/**
 * Quick fix that adds a @Suppress annotation to a composable function
 * to suppress stability warnings.
 */
internal class AddSuppressAnnotationFix(
  private val suppressName: String,
) : LocalQuickFix {

  override fun getFamilyName(): String = "Suppress stability warning"

  override fun getName(): String = "Add @Suppress(\"$suppressName\")"

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    val element = descriptor.psiElement
    val function = element.parent as? KtNamedFunction ?: return

    val existingSuppress = function.annotationEntries.find {
      it.shortName?.asString() == "Suppress"
    }

    if (existingSuppress != null) {
      addToExistingSuppress(project, existingSuppress, suppressName)
    } else {
      addNewSuppressAnnotation(project, function, suppressName)
    }
  }

  private fun addNewSuppressAnnotation(
    project: Project,
    function: KtNamedFunction,
    suppressName: String,
  ) {
    val factory = KtPsiFactory(project)
    val annotation = factory.createAnnotationEntry("@Suppress(\"$suppressName\")")

    val modifierList = function.modifierList
    if (modifierList != null) {
      modifierList.addBefore(annotation, modifierList.firstChild)
    } else {
      val newModifierList = factory.createModifierList("@Suppress(\"$suppressName\")")
      function.addBefore(newModifierList, function.funKeyword)
    }
  }

  private fun addToExistingSuppress(
    project: Project,
    suppress: org.jetbrains.kotlin.psi.KtAnnotationEntry,
    newSuppressName: String,
  ) {
    val factory = KtPsiFactory(project)
    val valueArguments = suppress.valueArgumentList?.arguments ?: emptyList()
    val alreadyExists = valueArguments.any { arg ->
      val text = arg.getArgumentExpression()?.text?.trim('"') ?: ""
      text == newSuppressName
    }

    if (alreadyExists) {
      return // Already suppressed
    }

    // Build new argument list
    val existingArgs = valueArguments.joinToString(", ") {
      it.getArgumentExpression()?.text ?: ""
    }

    val newArgs = if (existingArgs.isEmpty()) {
      "\"$newSuppressName\""
    } else {
      "$existingArgs, \"$newSuppressName\""
    }

    val newAnnotation = factory.createAnnotationEntry("@Suppress($newArgs)")
    suppress.replace(newAnnotation)
  }

  companion object {
    /**
     * Suppress name for non-skippable composables (normal mode).
     */
    const val NON_SKIPPABLE_COMPOSABLE = "NonSkippableComposable"

    /**
     * Suppress name for parameters compared by reference (strong skipping mode).
     */
    const val PARAMS_COMPARED_BY_REF = "ParamsComparedByRef"
  }
}
