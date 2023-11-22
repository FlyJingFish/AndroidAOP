package com.flyjingfish.android_aop_core.cut

import android.os.Looper
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.flyjingfish.android_aop_annotation.BasePointCut
import com.flyjingfish.android_aop_annotation.ProceedJoinPoint
import com.flyjingfish.android_aop_core.annotations.OnLifecycle
import com.flyjingfish.android_aop_core.utils.AppExecutors

class OnLifecycleCut : BasePointCut<OnLifecycle> {
    override fun invoke(joinPoint: ProceedJoinPoint, anno: OnLifecycle): Any? {
        if (Looper.getMainLooper() == Looper.myLooper()){
            invokeLifecycle(joinPoint, anno)
        }else{
            AppExecutors.mainThread().execute {
                invokeLifecycle(joinPoint, anno)
            }
        }
        return null
    }

    private fun invokeLifecycle(joinPoint: ProceedJoinPoint, annotation: OnLifecycle){
        val target = joinPoint.target
        if(target is FragmentActivity){
            target.lifecycle.addObserver(object : LifecycleEventObserver{
                override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                    if (event == annotation.value){
                        source.lifecycle.removeObserver(this)
                        joinPoint.proceed()
                    }
                }
            })
        }
    }
}