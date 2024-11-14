package com.flyjingfish.android_aop_annotation.utils

import com.flyjingfish.android_aop_annotation.ProceedJoinPoint
import com.flyjingfish.android_aop_annotation.base.BasePointCut
import com.flyjingfish.android_aop_annotation.base.BasePointCutCreator
import com.flyjingfish.android_aop_annotation.base.MatchClassMethod
import com.flyjingfish.android_aop_annotation.base.MatchClassMethodCreator
import com.flyjingfish.android_aop_annotation.base.OnBaseSuspendReturnListener
import java.lang.ref.ReferenceQueue
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

internal object AndroidAopBeanUtils {
    private val mBasePointCutMap = ConcurrentHashMap<String, BasePointCut<Annotation>>()
    private val mMatchClassMethodMap = ConcurrentHashMap<String, MatchClassMethod?>()
    private val mTargetReferenceMap = ConcurrentHashMap<String, KeyWeakReference<Any>>()
    private val mTargetMethodMap = ConcurrentHashMap<String, MethodMap>()
    private val mReturnListenerMap = ConcurrentHashMap<Any, MutableList<OnBaseSuspendReturnListener>>()
    private val mReturnKeyMap = ConcurrentHashMap<Any, Any>()
    private val mIgnoreOtherMap = ConcurrentHashMap<String, Boolean>()
    private val mTargetKeyReferenceQueue = ReferenceQueue<Any>()
    private val mSingleIO: ExecutorService = Executors.newSingleThreadExecutor()

    fun getCutClassCreator(annotationName: String): BasePointCutCreator? {
        return JoinAnnoCutUtils.getCutClassCreator(annotationName)
    }

    fun getMatchClassCreator(annotationName: String): MatchClassMethodCreator? {
        return JoinAnnoCutUtils.getMatchClassCreator(annotationName)
    }

    fun getBasePointCut(joinPoint: ProceedJoinPoint, annotationName : String,targetClassName:String, methodKey : String): BasePointCut<Annotation> {
        val key = "$targetClassName-${System.identityHashCode(joinPoint.target)}-$methodKey-$annotationName"
        var basePointCut: BasePointCut<Annotation>? = mBasePointCutMap[key]
        if (basePointCut == null) {
            basePointCut = getNewPointCut(annotationName)
            mBasePointCutMap[key] = basePointCut
            observeTarget(joinPoint.target, key)
        }else{
            removeWeaklyReachableObjectsOnIOThread()
        }
        return basePointCut
    }

    private fun getNewPointCut(annotationName: String): BasePointCut<Annotation> {
        val basePointCutCreator = JoinAnnoCutUtils.getCutClassCreator(annotationName)
        if (basePointCutCreator != null){
            return basePointCutCreator.newInstance() as BasePointCut<Annotation>
        }else{
            throw IllegalArgumentException("无法找到 $annotationName 的切面处理类")
        }
    }


    fun getMatchClassMethod(joinPoint: ProceedJoinPoint, cutClassName: String, targetClassName:String,methodKey : String): MatchClassMethod {
        val key = "$targetClassName-${System.identityHashCode(joinPoint.target)}-$methodKey-$cutClassName"
        var matchClassMethod: MatchClassMethod? = mMatchClassMethodMap[key]
        if (matchClassMethod == null) {
            matchClassMethod = getNewMatchClassMethod(cutClassName)
            mMatchClassMethodMap[key] = matchClassMethod
            observeTarget(joinPoint.target, key)
        }else{
            removeWeaklyReachableObjectsOnIOThread()
        }
        return matchClassMethod
    }

    private fun getNewMatchClassMethod(clsName: String): MatchClassMethod {
        val matchClassMethodCreator = JoinAnnoCutUtils.getMatchClassCreator(clsName)
        if (matchClassMethodCreator != null){
            return matchClassMethodCreator.newInstance()
        }else{
            throw IllegalArgumentException("无法找到 $clsName 的切面处理类")
        }
    }

    fun getMethodMapCache(key: String): MethodMap? {
        val methodMap = mTargetMethodMap[key]
        if (methodMap != null){
            removeWeaklyReachableObjectsOnIOThread()
        }
        return methodMap
    }

    fun putMethodMapCache(key: String, methodMap:MethodMap, target:Any?) {
        mTargetMethodMap[key] = methodMap
        observeTarget(target,key)
    }

    private fun observeTarget(target : Any?,key :String){
        mSingleIO.execute{
            if (target != null){
                val weakReference = KeyWeakReference(target,mTargetKeyReferenceQueue,key)
                mTargetReferenceMap[key] = weakReference
            }
            removeWeaklyReachableObjects()
        }
    }

    private fun removeWeaklyReachableObjectsOnIOThread(){
        mSingleIO.execute{
            removeWeaklyReachableObjects()
        }
    }

    private fun removeWeaklyReachableObjects() {
        var ref: KeyWeakReference<*>?
        do {
            ref = mTargetKeyReferenceQueue.poll() as KeyWeakReference<*>?
            if (ref != null) {
                mTargetReferenceMap.remove(ref.key)
                mBasePointCutMap.remove(ref.key)
                mMatchClassMethodMap.remove(ref.key)
                mTargetMethodMap.remove(ref.key)
            }
        } while (ref != null)
    }

    fun addSuspendReturnListener(key:Any,onSuspendReturnListener: OnBaseSuspendReturnListener){
        var list = mReturnListenerMap[key]
        if (list == null){
            list = mutableListOf()
            mReturnListenerMap[key] = list
        }
        list.add(onSuspendReturnListener)
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