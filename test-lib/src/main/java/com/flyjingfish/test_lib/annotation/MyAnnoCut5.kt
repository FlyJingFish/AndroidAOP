package com.flyjingfish.test_lib.annotation

import android.util.Log
import com.flyjingfish.android_aop_annotation.ProceedJoinPoint
import com.flyjingfish.android_aop_annotation.ProceedJoinPointSuspend
import com.flyjingfish.android_aop_annotation.ProceedReturn
import com.flyjingfish.android_aop_annotation.base.BasePointCutSuspend
import com.flyjingfish.android_aop_annotation.base.OnSuspendReturnListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.Thread.sleep

class MyAnnoCut5 : BasePointCutSuspend<MyAnno5> {
    override fun invoke(joinPoint: ProceedJoinPoint, anno: MyAnno5): Any? {
        Log.e("MyAnnoCut5", "====invoke=====")
        return joinPoint.proceed()
    }
    override suspend fun invokeSuspend(joinPoint: ProceedJoinPointSuspend, anno: MyAnno5){
        withContext(Dispatchers.IO) {
            Log.e("MyAnnoCut5", "====invokeSuspend=====")
//            if (joinPoint.target is MainActivity) {
//                (joinPoint.target as MainActivity).setLogcat("MyAnnoCut3====invokeSuspend=====")
//                val num = (joinPoint.target as MainActivity).getData2(1)
//                (joinPoint.target as MainActivity).setLogcat("MyAnnoCut3====invokeSuspend=====num=$num")
//            }
            sleep(2000)
            Log.e("MyAnnoCut5", "====invokeSuspend=====2")
            joinPoint.proceed(object : OnSuspendReturnListener {
                override fun onReturn(proceedReturn: ProceedReturn): Any? {
                    val  result = proceedReturn.proceed();
                    Log.e("MyAnnoCut5", "====onReturn=====result=$result")
                    return (result as Int)+100
                }

            })
        }

    }
}