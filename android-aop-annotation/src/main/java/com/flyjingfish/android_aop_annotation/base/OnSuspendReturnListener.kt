package com.flyjingfish.android_aop_annotation.base

import com.flyjingfish.android_aop_annotation.ProceedReturn

interface OnSuspendReturnListener {
    fun onReturn(proceedReturn: ProceedReturn):Any?
}