package com.flyjingfish.test_lib.replace

import android.util.Log
import com.flyjingfish.android_aop_annotation.anno.AndroidAopReplaceClass
import com.flyjingfish.android_aop_annotation.anno.AndroidAopReplaceMethod

@AndroidAopReplaceClass("android.util.Log")
class ReplaceLog {
    companion object{
        @AndroidAopReplaceMethod("int e(java.lang.String,java.lang.String)")
        @JvmStatic
        fun e( tag:String, msg:String) :Int{
            return Log.e(tag, "ReplaceLog-$msg")
        }
    }
}