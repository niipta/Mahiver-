-keepnames class com.example.data.ai.Content
-if class com.example.data.ai.Content
-keep class com.example.data.ai.ContentJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
