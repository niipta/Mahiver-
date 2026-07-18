-keepnames class com.example.data.ai.GenerateContentRequest
-if class com.example.data.ai.GenerateContentRequest
-keep class com.example.data.ai.GenerateContentRequestJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
-if class com.example.data.ai.GenerateContentRequest
-keepnames class kotlin.jvm.internal.DefaultConstructorMarker
-keepclassmembers class com.example.data.ai.GenerateContentRequest {
    public synthetic <init>(java.util.List,com.example.data.ai.GenerationConfig,com.example.data.ai.Content,int,kotlin.jvm.internal.DefaultConstructorMarker);
}
