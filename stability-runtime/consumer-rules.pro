# Keep all classes and functions annotated with @TraceRecomposition
# These are transformed by the compiler plugin and must not be obfuscated
-keep @com.skydoves.compose.stability.runtime.TraceRecomposition class * { *; }
-keep class * {
    @com.skydoves.compose.stability.runtime.TraceRecomposition *;
}
-keepclassmembers class * {
    @com.skydoves.compose.stability.runtime.TraceRecomposition *;
}

# Keep the TraceRecomposition annotation itself
-keep @interface com.skydoves.compose.stability.runtime.TraceRecomposition

# Keep all stability runtime annotations
-keep @interface com.skydoves.compose.stability.runtime.StableForAnalysis
-keep @interface com.skydoves.compose.stability.runtime.SkipStabilityAnalysis
-keep @interface com.skydoves.compose.stability.runtime.IgnoreStabilityReport

# Keep RecompositionTracker and related classes used by injected code
-keep class com.skydoves.compose.stability.runtime.RecompositionTracker { *; }
-keep class com.skydoves.compose.stability.runtime.** { *; }

# Keep all stability info data classes
-keep class com.skydoves.compose.stability.runtime.StabilityInfo { *; }
-keep class com.skydoves.compose.stability.runtime.ComposableInfo { *; }
-keep class com.skydoves.compose.stability.runtime.ParameterInfo { *; }
