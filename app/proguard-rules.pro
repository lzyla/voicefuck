# Kotlinx Serialization: keep serializers of DTOs used with Retrofit.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class com.aienglishcoach.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Retrofit interfaces are used reflectively.
-keepattributes Signature, Exceptions
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
