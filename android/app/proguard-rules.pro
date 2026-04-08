# MegaBot ProGuard Rules

# Keep Rhino JS Engine
-keep class org.mozilla.javascript.** { *; }
-dontwarn org.mozilla.javascript.**

# Keep Socket.IO
-keep class io.socket.** { *; }
-dontwarn io.socket.**

# Keep OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }

# Keep Gson
-keep class com.google.gson.** { *; }
-keepattributes Signature
-keepattributes *Annotation*

# Keep Room entities
-keep class com.megabot.data.local.db.entity.** { *; }

# Keep Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# Keep coroutines
-dontwarn kotlinx.coroutines.**
