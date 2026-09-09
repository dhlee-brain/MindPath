# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# --- MindPath 프로젝트 전용 규칙 ---

# Room: 엔티티 클래스/필드는 컬럼명과 매핑되므로 이름이 바뀌면 안 됨.
# (Room 라이브러리 자체 consumer-rules로도 대부분 커버되지만 명시적으로 방어)
-keep class com.dhlee.mindpath.data.*Entity { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class *
-keep @androidx.room.Database class *