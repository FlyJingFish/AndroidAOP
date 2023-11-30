package com.flyjingfish.android_aop_annotation.utils

import com.flyjingfish.android_aop_annotation.ProceedJoinPoint
import com.flyjingfish.android_aop_annotation.base.BasePointCut
import com.flyjingfish.android_aop_annotation.base.MatchClassMethod
import java.util.concurrent.ConcurrentHashMap

internal object AndroidAopBeanUtils {
    private val basePointCutMap = ConcurrentHashMap<String, BasePointCut<Annotation>?>()
    private val matchClassMethodMap = ConcurrentHashMap<String, MatchClassMethod?>()

    fun getBasePointCut(joinPoint: ProceedJoinPoint, clsName: String,annotationName : String): BasePointCut<Annotation>? {
        val className = joinPoint.targetClass.name
        val methodName = joinPoint.targetMethod.name
        val key = "$className-${joinPoint.target}-$methodName-$annotationName"
        var basePointCut: BasePointCut<Annotation>? = basePointCutMap[key]
        if (basePointCut == null) {
            basePointCut = getNewPointCut(clsName)
            basePointCutMap[key] = basePointCut
        }
        return basePointCut
    }

    private fun getNewPointCut(clsName: String): BasePointCut<Annotation>? {
        val cls: Class<out BasePointCut<Annotation>> = try {
            Class.forName(clsName) as Class<out BasePointCut<Annotation>>
        } catch (e: ClassNotFoundException) {
            throw RuntimeException(e)
        }
        val basePointCut: BasePointCut<Annotation>?= if (cls != BasePointCut::class.java) {
            try {
                cls.newInstance()
            } catch (e: IllegalAccessException) {
                throw RuntimeException(e)
            } catch (e: InstantiationException) {
                throw RuntimeException(e)
            }
        }else{
            null
        }
        return basePointCut
    }


    fun getMatchClassMethod(joinPoint: ProceedJoinPoint, clsName: String): MatchClassMethod {
        val className = joinPoint.targetClass.name
        val methodName = joinPoint.targetMethod.name
        val key = "$className-${joinPoint.target}-$methodName-$clsName"
        var matchClassMethod: MatchClassMethod? = matchClassMethodMap[key]
        if (matchClassMethod == null) {
            matchClassMethod = getNewMatchClassMethod(clsName)
            matchClassMethodMap[key] = matchClassMethod
        }
        return matchClassMethod
    }

    private fun getNewMatchClassMethod(clsName: String): MatchClassMethod {
        val cls: Class<out MatchClassMethod> = try {
            Class.forName(clsName) as Class<out MatchClassMethod>
        } catch (e: ClassNotFoundException) {
            throw RuntimeException(e)
        }
        return cls.newInstance()
    }
}