package com.flyjingfish.android_aop_core.cut

import android.os.Looper
import com.flyjingfish.android_aop_annotation.BasePointCut
import com.flyjingfish.android_aop_annotation.ProceedJoinPoint
import com.flyjingfish.android_aop_core.annotations.MainThread
import com.flyjingfish.android_aop_core.utils.AppExecutors

class MainThreadCut : BasePointCut<MainThread> {
    override fun invoke(joinPoint: ProceedJoinPoint, anno: MainThread): Any? {
        if (Looper.getMainLooper() == Looper.myLooper()){
            joinPoint.proceed()
        }else{
            AppExecutors.mainThread().execute {
                try {
                    joinPoint.proceed()
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            }
        }
        return null
    }

}