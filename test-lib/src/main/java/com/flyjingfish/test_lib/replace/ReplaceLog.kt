package com.flyjingfish.test_lib.replace

import android.util.Log
import com.flyjingfish.android_aop_annotation.anno.AndroidAopReplaceClass
import com.flyjingfish.android_aop_annotation.anno.AndroidAopReplaceMethod
import java.util.concurrent.ConcurrentHashMap

@AndroidAopReplaceClass("android.util.Log")
object ReplaceLog {
    val LOG_MAP = StringBuffer()
    @AndroidAopReplaceMethod("int e(java.lang.String,java.lang.String)")
    @JvmStatic
    fun e( tag:String, msg:String) :Int{
        synchronized(LOG_MAP){
            LOG_MAP.append("tag=$tag,msg=$msg\n")
        }
        return Log.e(tag, "ReplaceLog-$msg")
    }
}