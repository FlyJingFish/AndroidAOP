package com.flyjingfish.android_aop_annotation

interface ProceedReturn : ProceedReturn2 {

    /**
     * 继续执行 suspend 函数的返回值代码块
     *
     * @return 返回 suspend 函数的返回值代码块的结果
     */
    fun proceed(): Any?
}