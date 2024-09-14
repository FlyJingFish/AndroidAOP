package com.flyjingfish.androidaop

import com.flyjingfish.android_aop_annotation.anno.AndroidAopReplaceClass
import com.flyjingfish.android_aop_annotation.anno.AndroidAopReplaceMethod
import com.flyjingfish.android_aop_annotation.proxy.ProxyMethod
import com.flyjingfish.android_aop_annotation.proxy.ProxyType

//@AndroidAopReplaceClass("com.flyjingfish.androidaop.ThirdActivity")
object ReplaceGetData3 {
    //注解参数唯一变化的返回类型 改为 suspend， 其他不变
    @AndroidAopReplaceMethod("suspend getData1(int)")
    @JvmStatic
    @ProxyMethod(proxyClass = ThirdActivity::class,type = ProxyType.METHOD)
    suspend fun getData1(activity: ThirdActivity, num: Int): Int {
        return activity.getData1(num)
    }

    @AndroidAopReplaceMethod("suspend getData11(int)")
    @JvmStatic
    @ProxyMethod(proxyClass = ThirdActivity::class,type = ProxyType.METHOD)
    suspend fun getData11(activity: ThirdActivity,num:Int) :Int{
        return activity.getData11(num)
    }


    @AndroidAopReplaceMethod("suspend getData2(int,int)")
    @JvmStatic
    @ProxyMethod(proxyClass = ThirdActivity::class,type = ProxyType.METHOD)
    suspend fun getData2(activity: ThirdActivity,num:Int,num2:Int) :Int{
        return activity.getData2(num,num2)
    }

    @AndroidAopReplaceMethod("suspend getData3(int,int)")
    @JvmStatic
    @ProxyMethod(proxyClass = ThirdActivity::class,type = ProxyType.METHOD)
    suspend fun getData3(activity: ThirdActivity,num:Int,num2:Int) :Int{
        return activity.getData3(num,num2)
    }
}