package com.flyjingfish.android_aop_core.cut

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.flyjingfish.android_aop_annotation.ProceedJoinPoint
import com.flyjingfish.android_aop_annotation.base.BasePointCut
import com.flyjingfish.android_aop_core.annotations.Permission
import com.flyjingfish.android_aop_core.listeners.OnRequestPermissionListener
import com.flyjingfish.android_aop_core.utils.AndroidAop

internal class PermissionCut : BasePointCut<Permission> {
    override fun invoke(joinPoint: ProceedJoinPoint, anno: Permission): Any? {
        if (AndroidAop.getOnPermissionsInterceptListener() == null){
            return joinPoint.proceed()
        }
        AndroidAop.getOnPermissionsInterceptListener()?.requestPermission(joinPoint,anno,object : OnRequestPermissionListener{
            override fun onCall(isResult: Boolean) {
                if (isResult){
                    val target = joinPoint.target
                    if (target is LifecycleOwner){
                        if (target.lifecycle.currentState == Lifecycle.State.DESTROYED){
                            return
                        }
                    }
                    joinPoint.proceed()
                }
            }

        })
        return null
    }

}