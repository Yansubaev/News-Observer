# Project-specific R8 rules. Room, Hilt, Retrofit, OkHttp, WorkManager and Gson
# ship their own consumer rules; only reflection done by this app is listed here.

# Gson maps API responses onto DTO fields by name via reflection. Keep the whole
# DTO packages: a field without @SerializedName would otherwise be renamed and
# silently stay null.
-keep class com.ians.observer.data.remote.**.dto.** { *; }

# Readable stack traces in Play Console. Upload build/outputs/mapping/release/mapping.txt
# together with each AAB to deobfuscate them.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
