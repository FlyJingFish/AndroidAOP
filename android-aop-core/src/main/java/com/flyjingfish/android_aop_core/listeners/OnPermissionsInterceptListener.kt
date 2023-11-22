package com.flyjingfish.android_aop_core.listeners

import com.flyjingfish.android_aop_annotation.ProceedJoinPoint
import com.flyjingfish.android_aop_core.annotations.Permission

interface OnPermissionsInterceptListener {
    fun requestPermission(
        joinPoint: ProceedJoinPoint,
        permission : Permission,
        call: OnRequestPermissionListener
    )
}