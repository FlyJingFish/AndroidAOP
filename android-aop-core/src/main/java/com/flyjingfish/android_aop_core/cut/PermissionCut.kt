package com.flyjingfish.android_aop_core.cut

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.flyjingfish.android_aop_annotation.ProceedJoinPoint
import com.flyjingfish.android_aop_annotation.ProceedJoinPointSuspend
import com.flyjingfish.android_aop_annotation.base.BasePointCutSuspend
import com.flyjingfish.android_aop_core.annotations.Permission
import com.flyjingfish.android_aop_core.listeners.OnRequestPermissionListener
import com.flyjingfish.android_aop_core.utils.AndroidAop
import com.flyjingfish.android_aop_core.utils.AppExecutors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.CountDownLatch

internal class PermissionCut : BasePointCutSuspend<Permission> {
    override fun invoke(joinPoint: ProceedJoinPoint, anno: Permission): Any? {
        if (AndroidAop.getOnPermissionsInterceptListener() == null){
            return joinPoint.proceed()
        }
        AndroidAop.getOnPermissionsInterceptListener()?.requestPermission(joinPoint,anno
        ) { isResult ->
            if (isResult) {
                val target = joinPoint.target
                if (target is LifecycleOwner){
                    if (target.lifecycle.currentState == Lifecycle.State.DESTROYED){
                        return@requestPermission
                    }
                }
                joinPoint.proceed()
            }
        }
        return null
    }

    override suspend fun invokeSuspend(joinPoint: ProceedJoinPointSuspend, anno: Permission) {
        if (AndroidAop.getOnPermissionsInterceptListener() == null){
            joinPoint.proceed()
        }else{
            withContext(Dispatchers.IO) {
                val countDownLatch = CountDownLatch(1)
                var isPermissionResult = false
                AppExecutors.mainThread().execute{
                    AndroidAop.getOnPermissionsInterceptListener()?.requestPermission(joinPoint,anno
                    ) { isResult ->
                        isPermissionResult = isResult
                        countDownLatch.countDown()
                    }
                }
                countDownLatch.await()
                if (isPermissionResult){
                    joinPoint.proceed()
                }else{
                    joinPoint.proceedIgnoreOther { null }
                }
            }
        }


    }

}