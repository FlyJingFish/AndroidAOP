-keep class * implements kotlin.coroutines.Continuation{
   kotlin.coroutines.Continuation getCompletion();
   <fields>;
}
-keep class * implements com.flyjingfish.android_aop_annotation.utils.InvokeMethods{
   <methods>;
}
-keepclasseswithmembers class * {
    @com.flyjingfish.android_aop_annotation.aop_anno.AopKeep <fields>;
}
-keepclasseswithmembers class * {
    @com.flyjingfish.android_aop_annotation.aop_anno.AopKeep <methods>;
}
-keep class com.flyjingfish.android_aop_annotation.utils.DebugAndroidAopInit{
   *;
}
-keep @com.flyjingfish.android_aop_annotation.anno.AndroidAopPointCut class * { *; }
-keep class com.flyjingfish.android_aop_core.utils.AnnotationInit{
   *;
}