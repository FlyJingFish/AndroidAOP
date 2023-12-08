package com.flyjingfish.android_aop_core.cut

import android.os.Handler
import android.os.Looper
import com.flyjingfish.android_aop_annotation.ProceedJoinPoint
import com.flyjingfish.android_aop_annotation.base.BasePointCut
import com.flyjingfish.android_aop_core.annotations.Delay
import com.flyjingfish.android_aop_core.annotations.Scheduled
import com.flyjingfish.android_aop_core.utils.AndroidAop
import com.flyjingfish.android_aop_core.utils.AppExecutors
import com.flyjingfish.android_aop_core.utils.Utils
import java.util.UUID
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

internal class DelayCut:BasePointCut<Delay> {
    override fun invoke(joinPoint: ProceedJoinPoint, anno: Delay): Any? {
        val stopRunnable : Runnable
        val uuid = UUID.randomUUID().toString()
        if (anno.isOnMainThread){
            val handler = Handler(Looper.getMainLooper())
            if (anno.id.isNotEmpty()){
                AppExecutors.scheduledHandlerMap()[anno.id] = handler
            }
            stopRunnable = Runnable{
                handler.removeCallbacksAndMessages(null)
            }
            val runnable = Runnable {
                if (anno.id.isNotEmpty()){
                    AppExecutors.scheduledHandlerMap().remove(anno.id)
                }
                handler.removeCallbacksAndMessages(null)
                joinPoint.proceed()
            }

            if (anno.delay > 0){
                handler.postDelayed(runnable,anno.delay)
            }else{
                handler.post(runnable)
            }
        }else{
            val executor = Executors.newScheduledThreadPool(1)
            if (anno.id.isNotEmpty()){
                AppExecutors.scheduledExecutorMap()[anno.id] = executor
            }
            stopRunnable = Runnable{
                executor.shutdown()
            }
            executor.schedule({
                if (anno.id.isNotEmpty()){
                    AppExecutors.scheduledExecutorMap().remove(anno.id)
                }
                joinPoint.proceed()
                executor.shutdown()
            },anno.delay,TimeUnit.MILLISECONDS)
        }
        AppExecutors.mainThread().execute {
            Utils.invokeLifecycle(joinPoint,stopRunnable,uuid)
        }
        return null
    }

}