# ML Kit rules
-keep class com.google.mlkit.** { *; }
-dontwarn com.google.mlkit.**

# Room rules
-keep class androidx.room.RoomDatabase { *; }
-dontwarn androidx.room.**

# Hilt rules
-keep class dagger.hilt.** { *; }

# Gson rules
-keep class com.google.gson.** { *; }
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.terminplaner.domain.model.** { *; }
