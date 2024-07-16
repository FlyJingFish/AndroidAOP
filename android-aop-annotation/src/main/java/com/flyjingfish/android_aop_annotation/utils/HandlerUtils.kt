package com.flyjingfish.android_aop_annotation.utils

object HandlerUtils {
    var handler:Handler?=null
    fun post(runnable: Runnable){
        if (handler == null){
            Class.forName("com.flyjingfish.android_aop_core.utils.SetHandler")
        }
        handler?.post(runnable)
    }
    interface Handler{
        fun post(runnable: Runnable)
    }
}