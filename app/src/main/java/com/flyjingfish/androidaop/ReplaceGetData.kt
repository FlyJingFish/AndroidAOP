package com.flyjingfish.androidaop

import android.util.Log
import com.flyjingfish.android_aop_annotation.anno.AndroidAopReplaceClass
import com.flyjingfish.android_aop_annotation.anno.AndroidAopReplaceMethod

@AndroidAopReplaceClass("com.flyjingfish.androidaop.MainActivity")
object ReplaceGetData {
    //注解参数唯一变化的返回类型 改为 suspend， 其他不变
    @AndroidAopReplaceMethod("suspend getData(int)")
    @JvmStatic
    //  这里函数定义写法规则依旧不变，只是多加一个 suspend 修饰
    suspend fun getData(mainActivity: MainActivity, num: Int): Int {
        Log.e("ReplaceGetData","getData")
        return mainActivity.getData(num + 1)
    }
}