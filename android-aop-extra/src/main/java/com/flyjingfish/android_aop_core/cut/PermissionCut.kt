package com.flyjingfish.android_aop_core.cut

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.flyjingfish.android_aop_annotation.ProceedJoinPoint
import com.flyjingfish.android_aop_annotation.ProceedJoinPointSuspend
import com.flyjingfish.android_aop_annotation.base.BasePointCutSuspend
import com.flyjingfish.android_aop_core.annotations.Permission
import com.flyjingfish.android_aop_core.utils.AndroidAop
import com.flyjingfish.android_aop_core.utils.AppExecutors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.CountDownLatch

internal class PermissionCut : BasePointCutSuspend<Permission> {
    override fun invoke(joinPoint: ProceedJoinPoint, anno: Permission): Any? {
        val onPermissionsInterceptListener = AndroidAop.getOnPermissionsInterceptListener()
            ?: return joinPoint.proceed()
        onPermissionsInterceptListener.requestPermission(
            joinPoint, anno
        ) { isResult ->
            if (isResult) {
                val target = joinPoint.target
                if (target is LifecycleOwner) {
                    if (target.lifecycle.currentState == Lifecycle.State.DESTROYED) {
                        return@requestPermission
                    }
                }
                joinPoint.proceed()
            }
        }
        return null
    }

    override suspend fun invokeSuspend(joinPoint: ProceedJoinPointSuspend, anno: Permission) {
        withContext(Dispatchers.IO) {
            val onPermissionsInterceptListener = AndroidAop.getOnPermissionsInterceptListener()
            if (onPermissionsInterceptListener == null) {
                joinPoint.proceed()
            } else {
                val countDownLatch = CountDownLatch(1)
                var isPermissionResult = false
                AppExecutors.mainThread().execute {
                    onPermissionsInterceptListener.requestPermission(
                        ProceedJoinPointProxy(joinPoint), anno
                    ) { isResult ->
                        isPermissionResult = isResult
                        countDownLatch.countDown()
                    }
                }
                countDownLatch.await()
                if (isPermissionResult) {
                    joinPoint.proceed()
                } else {
                    joinPoint.proceedIgnoreOther { null }
                }
            }

        }
    }
}