package com.flyjingfish.android_aop_annotation.utils

object HandlerUtils {
    var handler:Handler?=null
    fun post(runnable: Runnable){
        handler?.post(runnable)
    }
    interface Handler{
        fun post(runnable: Runnable)
    }
}