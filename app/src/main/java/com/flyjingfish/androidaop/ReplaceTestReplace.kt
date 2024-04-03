package com.flyjingfish.androidaop

import android.util.Log
import com.flyjingfish.android_aop_annotation.anno.AndroidAopReplaceClass
import com.flyjingfish.android_aop_annotation.anno.AndroidAopReplaceMethod
import com.flyjingfish.android_aop_annotation.enums.MatchType
import com.flyjingfish.androidaop.test.TestBaseReplace

@AndroidAopReplaceClass("com.flyjingfish.androidaop.test.TestBaseReplace")
object ReplaceTestReplace {
    @AndroidAopReplaceMethod("void test()")
    @JvmStatic
    fun getData(test: TestBaseReplace) {
        Log.e("ReplaceTestReplace","----new----")
        test.test()
    }
}