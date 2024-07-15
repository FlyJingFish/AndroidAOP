package com.flyjingfish.android_aop_annotation.base

import com.flyjingfish.android_aop_annotation.ProceedJoinPointSuspend
import com.flyjingfish.android_aop_annotation.ProceedReturn2

/**
 * 对于 suspend 修饰的函数，可通过这个接口修改返回值
 * 这个类只能在 [ProceedJoinPointSuspend] 中设置
 */
interface OnSuspendReturnListener2 : OnBaseSuspendReturnListener{
    /**
     * 此处的返回值就是 suspend 函数的返回值
     */
    fun onReturn(proceedReturn: ProceedReturn2):Any?
}