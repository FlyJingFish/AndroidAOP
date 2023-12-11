package com.flyjingfish.test_lib.annotation

import com.flyjingfish.android_aop_annotation.anno.AndroidAopPointCut
import com.flyjingfish.test_lib.mycut.MyAnnoCut2

@AndroidAopPointCut(MyAnnoCut2::class)
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER,
)
@Retention(AnnotationRetention.RUNTIME)
annotation class MyAnno2