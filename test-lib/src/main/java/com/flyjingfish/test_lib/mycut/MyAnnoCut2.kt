package com.flyjingfish.test_lib.mycut

import android.content.Context
import com.flyjingfish.android_aop_annotation.BasePointCut
import com.flyjingfish.android_aop_annotation.ProceedJoinPoint
import com.flyjingfish.test_lib.ToastUtils
import com.flyjingfish.test_lib.annotation.MyAnno2

class MyAnnoCut2 : BasePointCut<MyAnno2> {
    override operator fun invoke(joinPoint: ProceedJoinPoint, anno: MyAnno2): Any? {
        ToastUtils.makeText(joinPoint.target as Context,"进入自定义Kotlin注解切面")
        return joinPoint.proceed()
    }
}