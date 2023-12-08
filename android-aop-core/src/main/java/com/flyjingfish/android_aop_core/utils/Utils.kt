package com.flyjingfish.android_aop_core.utils

import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.flyjingfish.android_aop_annotation.ProceedJoinPoint

internal object Utils {
    /**
     * 获取简约的方法名
     *
     * @param joinPoint
     * @return
     */
    fun getMethodName(joinPoint: ProceedJoinPoint): String {
        val methodName = joinPoint.targetMethod.name //方法名
        return getClassName(joinPoint.targetClass) + "." + methodName
    }

    private fun getClassName(cls: Class<*>?): String {
        if (cls == null) {
            return "<UnKnow Class>"
        }
        return if (cls.isAnonymousClass) {
            getClassName(cls.enclosingClass)
        } else cls.simpleName
    }

    fun invokeLifecycle(joinPoint: ProceedJoinPoint, stopRunnable : Runnable,key :String) {
        when (val target = joinPoint.target) {
            is LifecycleOwner -> {
                addObserver(target, stopRunnable)
            }
            else -> {
                val args = joinPoint.args
                if (!args.isNullOrEmpty()) {
                    val arg1 = args[0]
                    if (arg1 is LifecycleOwner){
                        addObserver(arg1, stopRunnable)
                    }
                }else{
                    ObserveTargetUtils.observeTarget(joinPoint,key)
                }
            }
        }

    }

    private fun addObserver(
        lifecycleOwner: LifecycleOwner,
        stopRunnable : Runnable
    ) {
        when (lifecycleOwner) {
            is Fragment -> {
                lifecycleOwner.viewLifecycleOwner.lifecycle.addObserver(object :
                    LifecycleEventObserver {
                    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                        if (event == Lifecycle.Event.ON_DESTROY) {
                            source.lifecycle.removeObserver(this)
                            stopRunnable.run()
                        }
                    }
                })
            }

            else -> {
                lifecycleOwner.lifecycle.addObserver(object : LifecycleEventObserver {
                    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                        if (event == Lifecycle.Event.ON_DESTROY) {
                            source.lifecycle.removeObserver(this)
                            stopRunnable.run()
                        }
                    }
                })
            }
        }
    }
}