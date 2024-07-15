package com.flyjingfish.android_aop_annotation.base

import com.flyjingfish.android_aop_annotation.ProceedJoinPointSuspend

/**
 * 对于 suspend 修饰的函数，可通过这个接口修改返回值
 * 这个类只能在 [ProceedJoinPointSuspend] 中设置
 */
interface OnBaseSuspendReturnListener