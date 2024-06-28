package com.flyjingfish.androidaop.test2

import android.util.Log
import com.flyjingfish.android_aop_annotation.ProceedJoinPoint
import com.flyjingfish.android_aop_annotation.base.BasePointCutSuspend
import com.flyjingfish.androidaop.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MyAnnoCut5 : BasePointCutSuspend<MyAnno5> {
    override suspend fun invokeSuspend(joinPoint: ProceedJoinPoint, anno: MyAnno5): Any? {
        return withContext(Dispatchers.IO) {
            Log.e("MyAnnoCut5", "====invokeSuspend=====")
//            if (joinPoint.target is MainActivity) {
//                (joinPoint.target as MainActivity).setLogcat("MyAnnoCut3====invokeSuspend=====")
//                val num = (joinPoint.target as MainActivity).getData2(1)
//                (joinPoint.target as MainActivity).setLogcat("MyAnnoCut3====invokeSuspend=====num=$num")
//            }
            Thread.sleep(2000)
            Log.e("MyAnnoCut5", "====invokeSuspend=====2")
            joinPoint?.proceed()
        }

    }
}