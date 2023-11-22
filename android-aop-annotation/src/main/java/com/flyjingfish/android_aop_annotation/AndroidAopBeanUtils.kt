package com.flyjingfish.android_aop_annotation

import java.util.concurrent.ConcurrentHashMap

object AndroidAopBeanUtils {
    private val basePointCutMap = ConcurrentHashMap<String, BasePointCut<Annotation>?>()
    private val matchClassMethodMap = ConcurrentHashMap<String, MatchClassMethod?>()

    fun getBasePointCut(joinPoint: ProceedJoinPoint, clsName: String): BasePointCut<Annotation>? {
        val className = joinPoint.target.javaClass.name
        val methodName = joinPoint.targetMethod.name
        val key = "$className.$methodName"
        var basePointCut: BasePointCut<Annotation>?
        if ("".equals(key)) {
            basePointCut = getNewPointCut(clsName)
        } else {
            basePointCut = basePointCutMap[key]
            if (basePointCut == null) {
                basePointCut = getNewPointCut(clsName)
                basePointCutMap[key] = basePointCut
            }
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
        val className = joinPoint.target.javaClass.name
        val methodName = joinPoint.targetMethod.name
        val key = "$className.$methodName"
        var matchClassMethod: MatchClassMethod?
        if ("".equals(key)) {
            matchClassMethod = getNewMatchClassMethod(clsName)
        } else {
            matchClassMethod = matchClassMethodMap[key]
            if (matchClassMethod == null) {
                matchClassMethod = getNewMatchClassMethod(clsName)
                matchClassMethodMap[key] = matchClassMethod
            }
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