package com.flyjingfish.test_lib.annotation

import android.util.Log
import com.flyjingfish.android_aop_annotation.ProceedJoinPoint
import com.flyjingfish.android_aop_annotation.ProceedJoinPointSuspend
import com.flyjingfish.android_aop_annotation.ProceedReturn
import com.flyjingfish.android_aop_annotation.ProceedReturn2
import com.flyjingfish.android_aop_annotation.base.BasePointCutSuspend
import com.flyjingfish.android_aop_annotation.base.OnSuspendReturnListener2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.Thread.sleep

class MyAnnoCut4 : BasePointCutSuspend<MyAnno4> {
    override fun invoke(joinPoint: ProceedJoinPoint, anno: MyAnno4): Any? {
        Log.e("MyAnnoCut4", "====invoke=====")
        return super.invoke(joinPoint, anno)
    }
    override suspend fun invokeSuspend(joinPoint: ProceedJoinPointSuspend, anno: MyAnno4){
        withContext(Dispatchers.IO) {
            Log.e("MyAnnoCut4", "====invokeSuspend=====")
//            if (joinPoint.target is MainActivity) {
//                (joinPoint.target as MainActivity).setLogcat("MyAnnoCut3====invokeSuspend=====")
//                val num = (joinPoint.target as MainActivity).getData2(1)
//                (joinPoint.target as MainActivity).setLogcat("MyAnnoCut3====invokeSuspend=====num=$num")
//            }
            sleep(2000)
            Log.e("MyAnnoCut4", "====invokeSuspend=====2")
            joinPoint.proceed()
        }

    }
}