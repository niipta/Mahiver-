-keepnames class com.example.data.ai.Candidate
-if class com.example.data.ai.Candidate
-keep class com.example.data.ai.CandidateJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
