package com.flyjingfish.android_aop_annotation.utils

object AndroidAOPDebugUtils {
    fun init(){
        Class.forName("com.flyjingfish.android_aop_core.utils.AndroidAOPInit")
    }
    var isDebug : Boolean = false
}