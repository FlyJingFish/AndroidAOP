package com.flyjingfish.android_aop_core.utils

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object AppExecutors {
    /**
     * 单线程池
     */
    private val mSingleIO: ExecutorService = Executors.newSingleThreadExecutor()

    /**
     * 多线程池
     */
    private val mPoolIO: ExecutorService =
        Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())

    /**
     * 主线程
     */
    private val mMainThread: Executor = MainThreadExecutor()

    class MainThreadExecutor : Executor {
        private val mainThreadHandler = Handler(Looper.getMainLooper())
        override fun execute(command: Runnable) {
            mainThreadHandler.post(command)
        }
    }

    fun singleIO(): ExecutorService {
        return mSingleIO
    }

    fun poolIO(): ExecutorService {
        return mPoolIO
    }

    fun mainThread(): Executor {
        return mMainThread
    }

}