-keepnames class com.example.data.ai.Part
-if class com.example.data.ai.Part
-keep class com.example.data.ai.PartJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
