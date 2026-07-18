-keepnames class com.example.data.ai.GenerationConfig
-if class com.example.data.ai.GenerationConfig
-keep class com.example.data.ai.GenerationConfigJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
-if class com.example.data.ai.GenerationConfig
-keepnames class kotlin.jvm.internal.DefaultConstructorMarker
-keepclassmembers class com.example.data.ai.GenerationConfig {
    public synthetic <init>(java.lang.Float,java.lang.String,int,kotlin.jvm.internal.DefaultConstructorMarker);
}
