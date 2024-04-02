package com.flyjingfish.androidaop

import android.util.Log
import com.flyjingfish.android_aop_annotation.anno.AndroidAopReplaceClass
import com.flyjingfish.android_aop_annotation.anno.AndroidAopReplaceMethod
import com.flyjingfish.androidaop.test.TestBaseReplace

@AndroidAopReplaceClass("com.flyjingfish.androidaop.test.TestBaseReplace")
object ReplaceTestReplace {
    //注解参数唯一变化的返回类型 改为 suspend， 其他不变
    @AndroidAopReplaceMethod("void test()")
    @JvmStatic
    //  这里函数定义写法规则依旧不变，只是多加一个 suspend 修饰
    fun getData(test: TestBaseReplace) {
        Log.e("ReplaceTestReplace","----new----")
        test.test()
    }
}