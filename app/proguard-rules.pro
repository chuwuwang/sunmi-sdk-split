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

# Keep all classes and internal structures of the Sunmi SDK
# (to prevent reflection from failing)
-keep class com.sunmi.** { *; }

# Keep all interfaces and enums in the Sunmi SDK
-keep interface com.sunmi.** { *; }
-keep enum com.sunmi.** { *; }

# Keep all annotation information
# (the SDK may use annotations for configuration)
-keepattributes *Annotation*

# Keep fields annotated with annotations under the com.sunmi.annotation package
-keepclassmembers class * {
    @com.sunmi.annotation.** <fields>;
}