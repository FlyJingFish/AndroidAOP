package com.flyjingfish.android_aop_core.cut

import android.os.Looper
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.flyjingfish.android_aop_annotation.ProceedJoinPoint
import com.flyjingfish.android_aop_annotation.base.BasePointCut
import com.flyjingfish.android_aop_core.annotations.MainThread
import com.flyjingfish.android_aop_core.utils.AppExecutors

internal class MainThreadCut : BasePointCut<MainThread> {
    override fun invoke(joinPoint: ProceedJoinPoint, anno: MainThread): Any? {
        return if (Looper.getMainLooper() == Looper.myLooper()){
            joinPoint.proceed()
        }else{
            AppExecutors.mainThread().execute {
                val target = joinPoint.target
                if (target is LifecycleOwner){
                    if (target.lifecycle.currentState == Lifecycle.State.DESTROYED){
                        return@execute
                    }
                }
                joinPoint.proceed()
            }
            null
        }

    }

}