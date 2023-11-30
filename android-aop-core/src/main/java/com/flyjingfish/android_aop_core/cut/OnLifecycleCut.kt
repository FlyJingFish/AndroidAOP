package com.flyjingfish.android_aop_core.cut

import android.os.Looper
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.flyjingfish.android_aop_annotation.ProceedJoinPoint
import com.flyjingfish.android_aop_annotation.base.BasePointCut
import com.flyjingfish.android_aop_core.annotations.OnLifecycle
import com.flyjingfish.android_aop_core.utils.AppExecutors

class OnLifecycleCut : BasePointCut<OnLifecycle> {
    override fun invoke(joinPoint: ProceedJoinPoint, anno: OnLifecycle): Any? {
        if (Looper.getMainLooper() == Looper.myLooper()) {
            invokeLifecycle(joinPoint, anno)
        } else {
            AppExecutors.mainThread().execute {
                invokeLifecycle(joinPoint, anno)
            }
        }
        return null
    }

    private fun invokeLifecycle(joinPoint: ProceedJoinPoint, annotation: OnLifecycle) {
        when (val target = joinPoint.target) {
            is LifecycleOwner -> {
                addObserver(target,joinPoint, annotation)
            }
            else -> {
                val args = joinPoint.args
                if (!args.isNullOrEmpty()) {
                    val arg1 = args[0]
                    if (arg1 is LifecycleOwner){
                        addObserver(arg1,joinPoint, annotation)
                    }else{
                        joinPoint.proceed()
                    }
                }else{
                    joinPoint.proceed()
                }
            }
        }

    }

    private fun addObserver(
        lifecycleOwner: LifecycleOwner,
        joinPoint: ProceedJoinPoint,
        annotation: OnLifecycle
    ) {
        when (lifecycleOwner) {
            is Fragment -> {
                lifecycleOwner.viewLifecycleOwner.lifecycle.addObserver(object :
                    LifecycleEventObserver {
                    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                        if (event == annotation.value) {
                            source.lifecycle.removeObserver(this)
                            joinPoint.proceed()
                        }
                    }
                })
            }

            else -> {
                lifecycleOwner.lifecycle.addObserver(object : LifecycleEventObserver {
                    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                        if (event == annotation.value) {
                            source.lifecycle.removeObserver(this)
                            joinPoint.proceed()
                        }
                    }
                })
            }
        }
    }
}