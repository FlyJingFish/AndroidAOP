package com.flyjingfish.test_lib

import android.util.Log
import com.flyjingfish.android_aop_annotation.anno.AndroidAopReplaceClass
import com.flyjingfish.android_aop_annotation.anno.AndroidAopReplaceMethod
import com.flyjingfish.android_aop_annotation.enums.MatchType
import com.flyjingfish.test_lib.TestMatch

@AndroidAopReplaceClass(value = "com.flyjingfish.test_lib.TestMatch",type = MatchType.EXTENDS)
object ReplaceTestMatch {

    @AndroidAopReplaceMethod("<init>(int)")
    @JvmStatic
    fun getTestBean(testBean: TestMatch) : TestMatch{
        return TestMatch(2)
    }

    @AndroidAopReplaceMethod("void test1()")
    @JvmStatic
    fun test1(testBean: TestMatch) {
        Log.e("test1","ReplaceTestMatch==label="+testBean.label)
    }

}