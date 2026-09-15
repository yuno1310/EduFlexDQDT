# API models are populated reflectively by Gson and use field names as JSON keys.
# Preserve their names and constructors in minified release builds.
-keep class com.eduflex.android.model.** { *; }
