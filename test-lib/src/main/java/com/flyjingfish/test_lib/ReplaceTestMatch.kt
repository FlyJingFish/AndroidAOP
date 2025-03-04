package com.flyjingfish.test_lib

import android.util.Log
import com.flyjingfish.android_aop_annotation.anno.AndroidAopReplaceClass
import com.flyjingfish.android_aop_annotation.anno.AndroidAopReplaceMethod
import com.flyjingfish.android_aop_annotation.anno.AndroidAopReplaceNew
import com.flyjingfish.android_aop_annotation.enums.MatchType
import com.flyjingfish.test_lib.TestMatch

@AndroidAopReplaceClass(value = "com.flyjingfish.test_lib.TestMatch",type = MatchType.EXTENDS)
object ReplaceTestMatch {
    @AndroidAopReplaceNew
    @JvmStatic
    fun newTestMatch2(testBean: TestMatch3):TestMatch3{
        Log.e("ReplaceTestMatch","AndroidAopReplaceNew=$testBean")
        return testBean
    }

//    @AndroidAopReplaceMethod("<init>(int)")
//    @JvmStatic
//    fun getTestBean(testBean: TestMatch) : TestMatch{
//        return TestMatch(2)
//    }

    @AndroidAopReplaceMethod("void test1()")
    @JvmStatic
    fun test1(testBean: Any) {
        Log.e("test1","ReplaceTestMatch=${testBean.javaClass.name}===label="+(testBean as TestMatch3).label)

    }

}