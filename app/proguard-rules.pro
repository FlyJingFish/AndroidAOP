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

# AndroidAop必备混淆规则 -----start-----
#-keep class * {
#    @androidx.annotation.Keep <fields>;
#}
#
#-keep @com.flyjingfish.android_aop_core.annotations.* class * {*;}
#-keep @com.flyjingfish.android_aop_annotation.anno.* class * {*;}
#-keep class * {
#    @com.flyjingfish.android_aop_core.annotations.* <fields>;
#    @com.flyjingfish.android_aop_annotation.anno.* <fields>;
#}
#-keepclassmembers class * {
#    @com.flyjingfish.android_aop_core.annotations.* <methods>;
#    @com.flyjingfish.android_aop_annotation.anno.* <methods>;
#}
#
#-keepnames class * implements com.flyjingfish.android_aop_annotation.base.BasePointCut
#-keepnames class * implements com.flyjingfish.android_aop_annotation.base.MatchClassMethod
#-keep class * implements com.flyjingfish.android_aop_annotation.base.BasePointCut{
#    public <init>();
#}
#-keepclassmembers class * implements com.flyjingfish.android_aop_annotation.base.BasePointCut{
#    <methods>;
#}
#
#-keep class * implements com.flyjingfish.android_aop_annotation.base.MatchClassMethod{
#    public <init>();
#}
#-keepclassmembers class * implements com.flyjingfish.android_aop_annotation.base.MatchClassMethod{
#    <methods>;
#}

# AndroidAop必备混淆规则 -----end-----


# 你自定义的混淆规则 -----start-----
#-keep @com.flyjingfish.test_lib.annotation.* class * {*;}
#-keep class * {
#    @com.flyjingfish.test_lib.annotation.* <fields>;
#}
#-keepclassmembers class * {
#    @com.flyjingfish.test_lib.annotation.* <methods>;
#}
#-keepnames class * extends androidx.appcompat.app.AppCompatActivity{
#    void startActivity(...);
#}

# 你自定义的混淆规则 -----end-----


# AndroidAop必备混淆规则 -----start-----
#-keep class * {
#    @androidx.annotation.Keep <fields>;
#}
#
#-keepnames class * implements com.flyjingfish.android_aop_annotation.base.BasePointCut
#-keepnames class * implements com.flyjingfish.android_aop_annotation.base.MatchClassMethod
#-keep class * implements com.flyjingfish.android_aop_annotation.base.BasePointCut{
#    public <init>();
#}
#-keep class * implements com.flyjingfish.android_aop_annotation.base.MatchClassMethod{
#    public <init>();
#}

# AndroidAop必备混淆规则 -----end-----

#-printmapping proguard-map.txt