# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep Kavi classes
-keep class com.kavi.mobile.** { *; }

# Retrofit
-keepattributes Signature
-keepattributes Exceptions
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
