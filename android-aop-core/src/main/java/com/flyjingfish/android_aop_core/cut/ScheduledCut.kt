package com.flyjingfish.android_aop_core.cut

import android.os.Handler
import android.os.Looper
import com.flyjingfish.android_aop_annotation.ProceedJoinPoint
import com.flyjingfish.android_aop_annotation.base.BasePointCut
import com.flyjingfish.android_aop_core.annotations.Scheduled
import com.flyjingfish.android_aop_core.utils.AppExecutors
import com.flyjingfish.android_aop_core.utils.Utils
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

internal class ScheduledCut:BasePointCut<Scheduled> {
    override fun invoke(joinPoint: ProceedJoinPoint, anno: Scheduled): Any? {
        val stopRunnable : Runnable
        if (anno.isOnMainThread){
            val handler = Handler(Looper.getMainLooper())
            if (anno.id.isNotEmpty()){
                AppExecutors.scheduledHandlerMap()[anno.id] = handler
            }
            stopRunnable = Runnable{
                handler.removeCallbacksAndMessages(null)
            }
            val runnable = object :Runnable{
                private var count = 0
                override fun run() {
                    if (anno.repeatCount != Scheduled.INFINITE && count++ >= anno.repeatCount){
                        if (anno.id.isNotEmpty()){
                            AppExecutors.scheduledHandlerMap().remove(anno.id)
                        }
                        handler.removeCallbacksAndMessages(null)
                        return
                    }
                    joinPoint.proceed()
                    handler.postDelayed(this,anno.interval)
                }
            }

            if (anno.initialDelay > 0){
                handler.postDelayed(runnable,anno.initialDelay)
            }else{
                handler.post(runnable)
            }
        }else{
            val executor = Executors.newScheduledThreadPool(2)
            if (anno.id.isNotEmpty()){
                AppExecutors.scheduledExecutorMap()[anno.id] = executor
            }
            stopRunnable = Runnable{
                executor.shutdown()
            }
            executor.scheduleAtFixedRate(object :Runnable{
                private var count = 0
                override fun run() {
                    if (anno.repeatCount != Scheduled.INFINITE && count++ >= anno.repeatCount){
                        if (anno.id.isNotEmpty()){
                            AppExecutors.scheduledExecutorMap().remove(anno.id)
                        }
                        executor.shutdown()
                        return
                    }
                    joinPoint.proceed()
                }
            },anno.initialDelay,anno.interval, TimeUnit.MILLISECONDS)
        }
        AppExecutors.mainThread().execute {
            Utils.invokeLifecycle(joinPoint,stopRunnable)
        }
        return null
    }

}