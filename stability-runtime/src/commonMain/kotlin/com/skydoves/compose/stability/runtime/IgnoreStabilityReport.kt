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
 * Annotation to exclude a composable function from stability validation reports.
 *
 * When applied to a @Composable function, it will be excluded from:
 * - Stability dump files (.stability files)
 * - Stability validation checks
 *
 * This is useful for:
 * - Preview composables (already shown in Android Studio, not in production)
 * - Debug/test-only composables
 * - Experimental composables where stability is not yet important
 * - Internal composables that don't affect production performance
 *
 * Example:
 * ```kotlin
 * @IgnoreStabilityReport
 * @Preview
 * @Composable
 * fun UserCardPreview() {
 *   UserCard(user = User("John", 30))
 * }
 * ```
 *
 * @see TraceRecomposition
 * @see SkipStabilityAnalysis
 */
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FUNCTION)
public annotation class IgnoreStabilityReport
