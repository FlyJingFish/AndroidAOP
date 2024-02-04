package com.flyjingfish.androidaop

import android.util.Log
import com.flyjingfish.android_aop_annotation.anno.AndroidAopReplaceClass
import com.flyjingfish.android_aop_annotation.anno.AndroidAopReplaceMethod
import com.flyjingfish.androidaop.test.TestBean

@AndroidAopReplaceClass("com.flyjingfish.androidaop.test.TestBean")
object ReplaceTestBean {
    @AndroidAopReplaceMethod("void setName(java.lang.String)")
    @JvmStatic
    fun setName(testBean: TestBean, name: String) {
        Log.e("ReplaceTestBean","setName")
        testBean.name = name
    }

    @AndroidAopReplaceMethod("java.lang.String getName()")
    @JvmStatic
    fun getName(testBean: TestBean):String {
        Log.e("ReplaceTestBean","getName")
        return testBean.name
    }
}