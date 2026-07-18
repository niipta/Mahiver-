-keepnames class com.example.data.BackupData
-if class com.example.data.BackupData
-keep class com.example.data.BackupDataJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
-if class com.example.data.BackupData
-keepnames class kotlin.jvm.internal.DefaultConstructorMarker
-keepclassmembers class com.example.data.BackupData {
    public synthetic <init>(int,long,com.example.data.BackupSettings,java.util.List,java.util.List,java.util.List,java.util.List,java.util.List,java.util.List,java.util.List,java.util.List,int,kotlin.jvm.internal.DefaultConstructorMarker);
}
