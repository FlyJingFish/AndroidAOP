package com.flyjingfish.androidaop.testReplace

import android.util.Log
import com.flyjingfish.android_aop_annotation.anno.AndroidAopReplaceClass
import com.flyjingfish.android_aop_annotation.anno.AndroidAopReplaceMethod

@AndroidAopReplaceClass("com.flyjingfish.androidaop.testReplace.BeanH")
object ReplaceBeanH {
    @AndroidAopReplaceMethod("<init>(java.lang.Object,int,int)")
    @JvmStatic
    fun getBeanH(o: Any, num1: Int, num2: Int,clazz: Class<*>): BeanH {
        Log.e("ReplaceBeanH", "getBeanH")
        return BeanH(o,num1,num2)
    }

    @AndroidAopReplaceMethod("void test()")
    @JvmStatic
    fun test(testBean: BeanH) {
        Log.e("ReplaceBeanH", "test")
        testBean.test()
    }
}