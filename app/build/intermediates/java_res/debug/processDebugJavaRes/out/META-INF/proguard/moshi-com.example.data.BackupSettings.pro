-keepnames class com.example.data.BackupSettings
-if class com.example.data.BackupSettings
-keep class com.example.data.BackupSettingsJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
-if class com.example.data.BackupSettings
-keepnames class kotlin.jvm.internal.DefaultConstructorMarker
-keepclassmembers class com.example.data.BackupSettings {
    public synthetic <init>(java.lang.String,boolean,int,kotlin.jvm.internal.DefaultConstructorMarker);
}
