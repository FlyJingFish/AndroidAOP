package com.flyjingfish.test_lib.mycut

@Target(
    AnnotationTarget.VALUE_PARAMETER
)
@Retention(
    AnnotationRetention.RUNTIME
)
annotation class TestParams(val value:String)