package com.flyjingfish.android_aop_annotation.utils

import com.flyjingfish.android_aop_annotation.base.BasePointCutCreator
import com.flyjingfish.android_aop_annotation.base.MatchClassMethodCreator
import com.flyjingfish.android_aop_annotation.base.OnBaseSuspendReturnListener
import java.util.concurrent.ConcurrentHashMap

internal object AndroidAopBeanUtils {
    private val mReturnListenerMap = ConcurrentHashMap<Any, MutableList<OnBaseSuspendReturnListener>>()
    private val mReturnKeyMap = ConcurrentHashMap<Any, Any>()
    private val mIgnoreOtherMap = ConcurrentHashMap<String, Boolean>()
    private val mReturnObjectPool = AutoCleaningObjectPool(
        creator = { arrayOfNulls<Any>(1) },
        maxCacheSize = 10,
        maxIdleTimeMillis = 30_000
    )

    fun borrowReturnObject():Array<Any?>{
        return mReturnObjectPool.borrow()
    }

    fun releaseReturnObject(o : Array<Any?>):Any?{
        val obj = o[0]
        mReturnObjectPool.release(o)
        return obj
    }

    fun getCutClassCreator(annotationName: String): BasePointCutCreator? {
        return JoinAnnoCutUtils.getCutClassCreator(annotationName)
    }

    fun getMatchClassCreator(annotationName: String): MatchClassMethodCreator? {
        return JoinAnnoCutUtils.getMatchClassCreator(annotationName)
    }

    fun addSuspendReturnListener(key:Any,onSuspendReturnListener: OnBaseSuspendReturnListener){
        val list = try {
            mReturnListenerMap.computeIfAbsent(key) { mutableListOf() }
        } catch (e: Throwable) {
            synchronized(mReturnListenerMap){
                var l = mReturnListenerMap[key]
                if (l == null){
                    l = mutableListOf()
                    mReturnListenerMap[key] = l
                }
                l
            }
        }

        synchronized(list){
            list.add(onSuspendReturnListener)
        }
    }

    fun getSuspendReturnListeners(key:Any?):MutableList<OnBaseSuspendReturnListener>?{
        if (key == null){
            return null
        }
        val listeners = mReturnListenerMap[key]
        if (listeners == null){
            val otherKey = mReturnKeyMap[key]
            if (otherKey != null){
                return mReturnListenerMap[otherKey]
            }
        }
        return listeners
    }

    fun saveReturnKey(key1: Any,key2: Any){
        mReturnKeyMap[key1] = key2
        mReturnKeyMap[key2] = key1
    }

    fun removeReturnListener(key: Any?){
        if (key == null){
            return
        }
        val otherKey = mReturnKeyMap[key]
        mReturnListenerMap.remove(key)
        otherKey?.let {
            mReturnListenerMap.remove(it)
            mReturnKeyMap.remove(it)
        }

        mReturnKeyMap.remove(key)
    }

    fun setIgnoreOther(onSuspendReturnListener: OnBaseSuspendReturnListener){
        mIgnoreOtherMap[onSuspendReturnListener.toString()] = true
    }

    fun isIgnoreOther(onSuspendReturnListener: OnBaseSuspendReturnListener):Boolean{
        val result = mIgnoreOtherMap[onSuspendReturnListener.toString()] ?: false
        mIgnoreOtherMap.remove(onSuspendReturnListener.toString())
        return result
    }

    fun removeIgnoreOther(onSuspendReturnListener: OnBaseSuspendReturnListener){
        mIgnoreOtherMap.remove(onSuspendReturnListener.toString())
    }
}