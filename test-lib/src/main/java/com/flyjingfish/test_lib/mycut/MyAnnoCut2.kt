package com.flyjingfish.test_lib.mycut

import com.flyjingfish.android_aop_annotation.BasePointCut
import com.flyjingfish.android_aop_annotation.ProceedJoinPoint
import com.flyjingfish.test_lib.annotation.MyAnno2

class MyAnnoCut2 : BasePointCut<MyAnno2> {
    override operator fun invoke(joinPoint: ProceedJoinPoint, anno: MyAnno2): Any? {
        return null
    }
}