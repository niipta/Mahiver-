-keepnames class com.example.data.ai.DailyPlanResponse
-if class com.example.data.ai.DailyPlanResponse
-keep class com.example.data.ai.DailyPlanResponseJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
