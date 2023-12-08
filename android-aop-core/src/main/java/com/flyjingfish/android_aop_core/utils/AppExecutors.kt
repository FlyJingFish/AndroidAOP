package com.flyjingfish.android_aop_core.utils

import android.os.Handler
import android.os.Looper
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService

internal object AppExecutors {
    /**
     * 单线程池
     */
    private val mSingleIO: ExecutorService by lazy {
        Executors.newSingleThreadExecutor()
    }

    /**
     * 多线程池
     */
    private val mPoolIO: ExecutorService by lazy {
        Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
    }

    private val mScheduledHandlerMap by lazy {
        ConcurrentHashMap<String,Handler>()
    }

    private val mScheduledExecutorMap by lazy {
        ConcurrentHashMap<String,ScheduledExecutorService>()
    }

    /**
     * 主线程
     */
    private val mMainThread: MainThreadExecutor by lazy {
        MainThreadExecutor()
    }

    class MainThreadExecutor : Executor {
        private val mainThreadHandler = Handler(Looper.getMainLooper())
        override fun execute(command: Runnable) {
            if (Looper.getMainLooper() != Looper.myLooper()){
                mainThreadHandler.post(command)
            }else{
                command.run()
            }
        }

        fun stopTask(runnable: Runnable){
            mainThreadHandler.removeCallbacks(runnable)
        }
    }

    fun singleIO(): ExecutorService {
        return mSingleIO
    }

    fun poolIO(): ExecutorService {
        return mPoolIO
    }

    fun mainThread(): MainThreadExecutor {
        return mMainThread
    }

    fun scheduledHandlerMap(): ConcurrentHashMap<String, Handler> {
        return mScheduledHandlerMap
    }

    fun scheduledExecutorMap(): ConcurrentHashMap<String, ScheduledExecutorService> {
        return mScheduledExecutorMap
    }

}