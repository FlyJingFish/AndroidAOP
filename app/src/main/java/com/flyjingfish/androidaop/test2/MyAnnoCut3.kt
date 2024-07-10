package com.flyjingfish.androidaop.test2

import android.util.Log
import com.flyjingfish.android_aop_annotation.ProceedJoinPoint
import com.flyjingfish.android_aop_annotation.ProceedReturn
import com.flyjingfish.android_aop_annotation.base.BasePointCutSuspend
import com.flyjingfish.android_aop_annotation.base.OnSuspendReturnListener
import com.flyjingfish.androidaop.MainActivity
import com.flyjingfish.androidaop.MyApp
import com.flyjingfish.test_lib.ToastUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MyAnnoCut3 : BasePointCutSuspend<MyAnno3> {
    override fun invoke(joinPoint: ProceedJoinPoint, anno: MyAnno3): Any? {
        Log.e("MyAnnoCut3", "====invoke=====")
        return super.invoke(joinPoint, anno)
    }

    override suspend fun invokeSuspend(joinPoint: ProceedJoinPoint, anno: MyAnno3) {
        withContext(Dispatchers.Main) {
            Log.e("MyAnnoCut3", "====invokeSuspend=====")
//            if (joinPoint.target is MainActivity) {
//                (joinPoint.target as MainActivity).setLogcat("MyAnnoCut3====invokeSuspend=====")
//                val num = (joinPoint.target as MainActivity).getData2(1)
//                (joinPoint.target as MainActivity).setLogcat("MyAnnoCut3====invokeSuspend=====num=$num")
//            }
            ToastUtils.makeText(MyApp.INSTANCE,"==MyAnnoCut3==")
            joinPoint.proceed(object :OnSuspendReturnListener{
                override fun onReturn(proceedReturn: ProceedReturn): Any? {
                    Log.e("MyAnnoCut3", "====onReturn=====")
                    return (proceedReturn.proceed() as Int)+100
                }

            })
        }

    }
}