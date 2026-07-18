-keepnames class com.example.data.SubjectJson
-if class com.example.data.SubjectJson
-keep class com.example.data.SubjectJsonJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
-if class com.example.data.SubjectJson
-keepnames class kotlin.jvm.internal.DefaultConstructorMarker
-keepclassmembers class com.example.data.SubjectJson {
    public synthetic <init>(java.lang.String,java.lang.String,java.lang.String,java.util.List,int,kotlin.jvm.internal.DefaultConstructorMarker);
}
