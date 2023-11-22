package com.flyjingfish.android_aop_core.cut

import com.flyjingfish.android_aop_annotation.BasePointCut
import com.flyjingfish.android_aop_annotation.ProceedJoinPoint
import com.flyjingfish.android_aop_core.annotations.Permission
import com.flyjingfish.android_aop_core.listeners.OnRequestPermissionListener
import com.flyjingfish.android_aop_core.utils.AndroidAop

class PermissionCut : BasePointCut<Permission> {
    override fun invoke(joinPoint: ProceedJoinPoint, anno: Permission): Any? {
        if (AndroidAop.getOnPermissionsInterceptListener() == null){
            return joinPoint.proceed()
        }
        AndroidAop.getOnPermissionsInterceptListener()?.requestPermission(joinPoint,anno,object : OnRequestPermissionListener{
            override fun onCall(isResult: Boolean) {
                if (isResult){
                    joinPoint.proceed()
                }
            }

        })
        return null
    }

}