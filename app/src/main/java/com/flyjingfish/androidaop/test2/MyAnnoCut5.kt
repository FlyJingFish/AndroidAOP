package com.flyjingfish.androidaop.test2

import android.util.Log
import com.flyjingfish.android_aop_annotation.ProceedJoinPoint
import com.flyjingfish.android_aop_annotation.ProceedReturn
import com.flyjingfish.android_aop_annotation.base.BasePointCutSuspend
import com.flyjingfish.android_aop_annotation.base.OnSuspendReturnListener
import com.flyjingfish.androidaop.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.Thread.sleep

class MyAnnoCut5 : BasePointCutSuspend<MyAnno5> {
    override fun invoke(joinPoint: ProceedJoinPoint, anno: MyAnno5): Any? {
        Log.e("MyAnnoCut5", "====invoke=====")
        return joinPoint.proceed(object : OnSuspendReturnListener {
            override fun onReturn(proceedReturn: ProceedReturn): Any? {
                Log.e("MyAnnoCut5", "====onReturn=====")
                return (proceedReturn.proceed() as Int)+100
            }

        })
    }
    override suspend fun invokeSuspend(joinPoint: ProceedJoinPoint, anno: MyAnno5){
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
                    Log.e("MyAnnoCut5", "====onReturn=====")
                    return (proceedReturn.proceed() as Int)+100
                }

            })
        }

    }
}