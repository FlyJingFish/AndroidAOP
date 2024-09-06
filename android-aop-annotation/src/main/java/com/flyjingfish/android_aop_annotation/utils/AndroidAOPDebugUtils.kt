package com.flyjingfish.android_aop_annotation.utils

object AndroidAOPDebugUtils {
    internal fun init(){
        try {
            Class.forName("com.flyjingfish.android_aop_core.utils.AnnotationInit")
        } catch (_: Throwable) {
        }
        init = true
    }
    private var init = false
    var isApkDebug : Boolean = false
        set(value) {
            if (init){
                throw IllegalStateException("isApkDebug can only be set once")
            }
            field = value
        }
}