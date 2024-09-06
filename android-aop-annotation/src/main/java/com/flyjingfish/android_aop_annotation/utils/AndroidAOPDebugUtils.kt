package com.flyjingfish.android_aop_annotation.utils

object AndroidAOPDebugUtils {
    internal fun init(){
        try {
            Class.forName("com.flyjingfish.android_aop_core.utils.AndroidAOPInit")
        } catch (_: Throwable) {
        }
    }
    var isDebug : Boolean = false
}