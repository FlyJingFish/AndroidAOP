package com.flyjingfish.androidaop.test2

import android.util.Log
import com.flyjingfish.android_aop_annotation.AopMethod
import com.flyjingfish.android_aop_annotation.ProceedJoinPoint
import com.flyjingfish.android_aop_annotation.base.BasePointCut
import com.flyjingfish.android_aop_annotation.base.BasePointCutSuspend
import com.flyjingfish.androidaop.MainActivity
import com.flyjingfish.androidaop.MyApp
import com.flyjingfish.test_lib.ToastUtils

class MyAnnoCut3 : BasePointCutSuspend<MyAnno3> {
    override suspend fun invokeSuspend(joinPoint: ProceedJoinPoint, anno: MyAnno3): Any? {
        Log.e("MyAnnoCut3", "====invokeSuspend=====")
        if (joinPoint.target is MainActivity) {
            (joinPoint.target as MainActivity).getData3(joinPoint.args?.get(0) as Int)
        }
        return joinPoint.proceedSuspend()
    }
}