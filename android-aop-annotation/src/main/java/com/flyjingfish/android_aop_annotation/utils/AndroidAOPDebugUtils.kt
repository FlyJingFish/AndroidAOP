package com.flyjingfish.android_aop_annotation.utils

object AndroidAOPDebugUtils {
    internal fun init(){
        try {
            Class.forName("com.flyjingfish.android_aop_core.utils.AnnotationInit")
        } catch (_: Throwable) {
        }
    }
    var isApkDebug : Boolean = false
}