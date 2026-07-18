-keepnames class com.example.data.SyllabusJson
-if class com.example.data.SyllabusJson
-keep class com.example.data.SyllabusJsonJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
-if class com.example.data.SyllabusJson
-keepnames class kotlin.jvm.internal.DefaultConstructorMarker
-keepclassmembers class com.example.data.SyllabusJson {
    public synthetic <init>(java.util.List,int,kotlin.jvm.internal.DefaultConstructorMarker);
}
