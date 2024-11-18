package com.flyjingfish.androidaop.testReplace

import com.flyjingfish.android_aop_annotation.anno.AndroidAopReplaceClass
import com.flyjingfish.android_aop_annotation.anno.AndroidAopReplaceMethod

//@AndroidAopReplaceClass("com.flyjingfish.androidaop.testReplace.BaseBean")
object ReplaceBaseBean {
    @AndroidAopReplaceMethod("<init>(int,int)")
    @JvmStatic
    fun getBaseBean(testBean: BaseBean) : BaseBean {
        return testBean
    }

    @AndroidAopReplaceMethod("void test()")
    @JvmStatic
    fun test(testBean: BaseBean){
        testBean.test()
    }
}