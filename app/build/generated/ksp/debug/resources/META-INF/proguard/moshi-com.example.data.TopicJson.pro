-keepnames class com.example.data.TopicJson
-if class com.example.data.TopicJson
-keep class com.example.data.TopicJsonJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
-if class com.example.data.TopicJson
-keepnames class kotlin.jvm.internal.DefaultConstructorMarker
-keepclassmembers class com.example.data.TopicJson {
    public synthetic <init>(java.lang.String,int,boolean,boolean,java.util.List,int,kotlin.jvm.internal.DefaultConstructorMarker);
}
