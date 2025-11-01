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
package com.skydoves.myapplication

import android.app.Application
import android.util.Log
import com.skydoves.compose.stability.runtime.ComposeStabilityAnalyzer
import com.skydoves.compose.stability.runtime.DefaultRecompositionLogger
import com.skydoves.compose.stability.runtime.RecompositionEvent
import com.skydoves.compose.stability.runtime.RecompositionLogger

/**
 * Example Application class showing how to configure RecompositionLogger.
 *
 * Don't forget to register this in AndroidManifest.xml:
 * ```xml
 * <application
 *     android:name=".MyApplication"
 *     ...>
 * ```
 */
class MyApplication : Application() {

  override fun onCreate() {
    super.onCreate()

    // Example 1: Enable default logger only in debug builds
    // This is the recommended setup for most apps
    ComposeStabilityAnalyzer.setEnabled(true) // Set to BuildConfig.DEBUG in production

    // Example 2: Use a custom logger (uncomment to use)
    // setupCustomLogger()

    // Example 3: Use analytics logger (uncomment to use)
    // setupAnalyticsLogger()
  }

  /**
   * Example of a custom logger that formats output differently.
   */
  private fun setupCustomLogger() {
    ComposeStabilityAnalyzer.setLogger(object : RecompositionLogger {
      override fun log(event: RecompositionEvent) {
        val message = buildString {
          append("🔄 Recomposition #${event.recompositionCount}")
          append(" - ${event.composableName}")
          if (event.tag.isNotEmpty()) {
            append(" [${event.tag}]")
          }
          appendLine()

          event.parameterChanges.forEach { change ->
            append("   • ${change.name}: ${change.type}")
            when {
              change.changed -> append(" ➡️ CHANGED")
              change.stable -> append(" ✅ STABLE")
              else -> append(" ⚠️ UNSTABLE")
            }
            appendLine()
          }

          if (event.unstableParameters.isNotEmpty()) {
            append("   ⚠️ Unstable: ${event.unstableParameters.joinToString()}")
          }
        }

        Log.d("CustomRecomposition", message)
      }
    })
  }

  /**
   * Example of an analytics logger that sends recomposition data to analytics.
   * Useful for tracking performance issues in production.
   */
  private fun setupAnalyticsLogger() {
    ComposeStabilityAnalyzer.setLogger(object : RecompositionLogger {
      override fun log(event: RecompositionEvent) {
        // Track excessive recompositions
        if (event.recompositionCount >= 10) {
          // Example: Send to Firebase Analytics
          // FirebaseAnalytics.getInstance(this).logEvent("excessive_recomposition") {
          //   param("composable", event.composableName)
          //   param("count", event.recompositionCount)
          //   param("unstable_params", event.unstableParameters.joinToString())
          //   param("tag", event.tag)
          // }

          Log.w(
            "RecompositionAnalytics",
            "Excessive recomposition detected: ${event.composableName} " +
              "(${event.recompositionCount} times)",
          )
        }
      }
    })
  }

  /**
   * Example of a filtering logger that only logs specific tags.
   * Useful when you want to focus on specific screens or features.
   */
  private fun setupFilteredLogger() {
    val tagsToLog = setOf("user-profile", "checkout", "performance")

    ComposeStabilityAnalyzer.setLogger(object : RecompositionLogger {
      override fun log(event: RecompositionEvent) {
        // Only log events with specific tags
        if (event.tag in tagsToLog || event.tag.isEmpty()) {
          // Use default logger for these tags
          val defaultLogger = DefaultRecompositionLogger()
          defaultLogger.log(event)
        }
      }
    })
  }

  /**
   * Example of a logger that writes to a file for later analysis.
   * Useful for collecting recomposition data over time.
   */
  private fun setupFileLogger() {
    ComposeStabilityAnalyzer.setLogger(object : RecompositionLogger {
      override fun log(event: RecompositionEvent) {
        try {
          val logFile = getExternalFilesDir(null)?.resolve("recomposition.log")
          logFile?.appendText(
            buildString {
              append("${System.currentTimeMillis()},")
              append("${event.composableName},")
              append("${event.tag},")
              append("${event.recompositionCount},")
              append("${event.unstableParameters.joinToString(";")}")
              appendLine()
            },
          )
        } catch (e: Exception) {
          Log.e("FileLogger", "Failed to write recomposition log", e)
        }
      }
    })
  }

  /**
   * Example of a threshold-based logger that only logs when
   * recomposition count exceeds a threshold.
   */
  private fun setupThresholdLogger() {
    val globalThreshold = 5

    ComposeStabilityAnalyzer.setLogger(object : RecompositionLogger {
      override fun log(event: RecompositionEvent) {
        if (event.recompositionCount >= globalThreshold) {
          Log.w(
            "RecompositionThreshold",
            "${event.composableName} recomposed ${event.recompositionCount} times! " +
              "Unstable params: ${event.unstableParameters}",
          )
        }
      }
    })
  }
}
