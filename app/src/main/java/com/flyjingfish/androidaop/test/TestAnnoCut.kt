package com.flyjingfish.androidaop.test

import android.util.Log
import com.flyjingfish.android_aop_annotation.ProceedJoinPoint
import com.flyjingfish.android_aop_annotation.base.BasePointCut
import com.flyjingfish.test_lib.annotation.MyAnno2

internal class TestAnnoCut : BasePointCut<TestAnno> {
    override fun invoke(joinPoint: ProceedJoinPoint, anno: TestAnno): Any? {
        Log.e("TestAnnoCut" , "=====>>>>=${anno}")

        return joinPoint.proceed()
    }
}