-keepnames class com.example.data.ai.GenerateContentResponse
-if class com.example.data.ai.GenerateContentResponse
-keep class com.example.data.ai.GenerateContentResponseJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
