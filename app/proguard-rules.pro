# Generated Proguard Rules — Chakuli

# LiteRT-LM — keep all native bridge classes
-keep class com.google.ai.edge.litertlm.** { *; }
-keepattributes *Annotation*

# Hilt — keep generated components
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.HiltAndroidApp { *; }

# Moshi — for agent JSON parsing
-keep class com.squareup.moshi.** { *; }
-keepclassmembers class ** {
    @com.squareup.moshi.FromJson <methods>;
    @com.squareup.moshi.ToJson <methods>;
}

# Kotlin serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# Protobuf - Keep all proto classes and fields
-keep class com.google.protobuf.** { *; }
-keep class * extends com.google.protobuf.GeneratedMessageLite { *; }
-keepclassmembers class * extends com.google.protobuf.GeneratedMessageLite {
    <fields>;
    <methods>;
}

# Keep DataStore proto classes specifically
-keep class com.bhanu.aegis.core.data.datastore.** { *; }
-keepclassmembers class com.bhanu.aegis.core.data.datastore.** {
    *;
}

# Compose - Fix fontWeightAdjustment crash on older Android versions
-dontwarn android.content.res.Configuration$fontWeightAdjustment

# Kotlin Reflect (needed for @Tool annotation processing at runtime)
-keep class kotlin.reflect.** { *; }
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations

# Keep all @Tool annotated classes
-keep @com.google.ai.edge.litertlm.Tool class * { *; }
-keepclassmembers class * {
    @com.google.ai.edge.litertlm.Tool *;
    @com.google.ai.edge.litertlm.ToolParam *;
}
