package com.flyjingfish.android_aop_annotation.utils

import com.flyjingfish.android_aop_annotation.ProceedJoinPoint
import com.flyjingfish.android_aop_annotation.base.BasePointCut
import com.flyjingfish.android_aop_annotation.base.MatchClassMethod
import java.lang.ref.ReferenceQueue
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

internal object AndroidAopBeanUtils {
    private val mBasePointCutMap = ConcurrentHashMap<String, BasePointCut<Annotation>?>()
    private val mMatchClassMethodMap = ConcurrentHashMap<String, MatchClassMethod?>()
    private val mTargetReferenceMap = ConcurrentHashMap<String, KeyWeakReference<Any>>()
    private val mTargetKeyReferenceQueue = ReferenceQueue<Any>()
    private val mSingleIO: ExecutorService = Executors.newSingleThreadExecutor()

    fun getCutClassName(className: String): String? {
        if (!JoinAnnoCutUtils.isInit()){
            MethodAnnoUtils.registerMap()
        }
        return JoinAnnoCutUtils.getCutClassName(className)
    }

    fun getBasePointCut(joinPoint: ProceedJoinPoint, clsName: String,annotationName : String): BasePointCut<Annotation>? {
        val className = joinPoint.targetClass.name
        val methodName = joinPoint.targetMethod.name
        val key = "$className-${joinPoint.target}-$methodName-$annotationName"
        var basePointCut: BasePointCut<Annotation>? = mBasePointCutMap[key]
        if (basePointCut == null) {
            basePointCut = getNewPointCut(clsName)
            mBasePointCutMap[key] = basePointCut
            observeTarget(joinPoint, key)
        }else{
            removeWeaklyReachableObjectsOnIOThread()
        }
        return basePointCut
    }



    private fun getNewPointCut(clsName: String): BasePointCut<Annotation> {
        val cls: Class<out BasePointCut<Annotation>> = try {
            Class.forName(clsName) as Class<out BasePointCut<Annotation>>
        } catch (e: ClassNotFoundException) {
            throw RuntimeException(e)
        }
        val basePointCut: BasePointCut<Annotation> = if (cls != BasePointCut::class.java) {
            cls.getDeclaredConstructor().newInstance()
        }else{
            throw IllegalArgumentException("切面处理类必须实现 BasePointCut 接口")
        }
        return basePointCut
    }


    fun getMatchClassMethod(joinPoint: ProceedJoinPoint, clsName: String): MatchClassMethod {
        val className = joinPoint.targetClass.name
        val methodName = joinPoint.targetMethod.name
        val key = "$className-${joinPoint.target}-$methodName-$clsName"
        var matchClassMethod: MatchClassMethod? = mMatchClassMethodMap[key]
        if (matchClassMethod == null) {
            matchClassMethod = getNewMatchClassMethod(clsName)
            mMatchClassMethodMap[key] = matchClassMethod
            observeTarget(joinPoint, key)
        }else{
            removeWeaklyReachableObjectsOnIOThread()
        }
        return matchClassMethod
    }

    private fun getNewMatchClassMethod(clsName: String): MatchClassMethod {
        val cls: Class<out MatchClassMethod> = try {
            Class.forName(clsName) as Class<out MatchClassMethod>
        } catch (e: ClassNotFoundException) {
            throw RuntimeException(e)
        }
        val matchClassMethod: MatchClassMethod = if (cls != MatchClassMethod::class.java) {
            cls.getDeclaredConstructor().newInstance()
        }else{
            throw IllegalArgumentException("切面处理类必须实现 MatchClassMethod 接口")
        }
        return matchClassMethod
    }

    private fun observeTarget(joinPoint: ProceedJoinPoint,key :String){
        mSingleIO.execute{
            synchronized(AndroidAopBeanUtils){
                val target = joinPoint.target
                if (target != null){
                    val weakReference = KeyWeakReference(target,mTargetKeyReferenceQueue,key)
                    mTargetReferenceMap[key] = weakReference
                }
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
        synchronized(AndroidAopBeanUtils){
            var ref: KeyWeakReference<*>?
            do {
                ref = mTargetKeyReferenceQueue.poll() as KeyWeakReference<*>?
                if (ref != null) {
                    mTargetReferenceMap.remove(ref.key)
                    mBasePointCutMap.remove(ref.key)
                    mMatchClassMethodMap.remove(ref.key)
                }
            } while (ref != null)
        }
    }
}