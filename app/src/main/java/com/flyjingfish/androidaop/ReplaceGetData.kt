package com.flyjingfish.androidaop

import android.util.Log
import com.flyjingfish.android_aop_annotation.anno.AndroidAopReplaceClass
import com.flyjingfish.android_aop_annotation.anno.AndroidAopReplaceMethod

@AndroidAopReplaceClass("com.flyjingfish.androidaop.MainActivity")
object ReplaceGetData {
    @AndroidAopReplaceMethod("suspend getData(int)")
    @JvmStatic
    //  因为被替换方法是静态的，所以参数类型及顺序和被替换方法一一对应
    suspend fun getData(mainActivity: MainActivity, num: Int): Int {
        Log.e("ReplaceGetData","getData")
        return mainActivity.getData(num + 1)
    }
}