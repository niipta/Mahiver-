# Keep Room entities and DAOs (KSP-generated code references them by name)
-keep class com.example.data.*Entity { *; }
-keep class com.example.data.*Dao { *; }

# Keep Moshi @JsonClass-generated adapters
-keep class @com.squareup.moshi.JsonClass * { *; }
-keep class **JsonAdapter { *; }

# Keep Firebase model classes (Firestore uses reflection)
-keep class com.example.data.** { *; }

# Keep Kotlin metadata
-keepattributes *Annotation*, InnerClasses, Signature, Exceptions
