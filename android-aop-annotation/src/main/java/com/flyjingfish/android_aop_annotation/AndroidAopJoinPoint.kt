package com.flyjingfish.android_aop_annotation

import com.flyjingfish.android_aop_annotation.ProceedJoinPoint.OnInvokeListener
import com.flyjingfish.android_aop_annotation.base.BasePointCut
import com.flyjingfish.android_aop_annotation.base.BasePointCutSuspend
import com.flyjingfish.android_aop_annotation.base.MatchClassMethod
import com.flyjingfish.android_aop_annotation.base.MatchClassMethodSuspend
import com.flyjingfish.android_aop_annotation.utils.AndroidAopBeanUtils.getBasePointCut
import com.flyjingfish.android_aop_annotation.utils.AndroidAopBeanUtils.getCutClassCreator
import com.flyjingfish.android_aop_annotation.utils.AndroidAopBeanUtils.getMatchClassCreator
import com.flyjingfish.android_aop_annotation.utils.AndroidAopBeanUtils.getMatchClassMethod
import com.flyjingfish.android_aop_annotation.utils.AndroidAopBeanUtils.getMethodMapCache
import com.flyjingfish.android_aop_annotation.utils.AndroidAopBeanUtils.putMethodMapCache
import com.flyjingfish.android_aop_annotation.utils.InvokeMethod
import com.flyjingfish.android_aop_annotation.utils.MethodMap
import java.lang.reflect.Method

class AndroidAopJoinPoint (
    clazz: Class<*>,
    target: Any?,
    originalMethodName: String,
    targetMethodName: String
){
    private val target: Any?
    private var targetClass: Class<*>? = null
    private var mArgs: Array<Any?>? = null
    private lateinit var mArgClasses: Array<Class<*>>
    private val targetMethodName: String
    private val originalMethodName: String
    private lateinit var targetMethod: Method
    private lateinit var originalMethod: Method
    private var cutMatchClassName: String? = null
    private var paramsKey: String? = null
    private var methodKey: String? = null
    private var targetClassName: String? = null
    private var invokeMethod: InvokeMethod? = null

    init {
        targetClassName = clazz.name
        this.target = target
        this.originalMethodName = originalMethodName
        this.targetMethodName = targetMethodName
        targetClass = clazz
    }

    private fun getProceedJoinPoint():ProceedJoinPoint{
        val targetAopMethod = AopMethod(originalMethod)
        val proceedJoinPoint = ProceedJoinPoint(targetClass!!, mArgs,targetAopMethod,target)
        proceedJoinPoint.setOriginalMethod(originalMethod)
        proceedJoinPoint.setTargetJavaMethod(targetMethod)
        proceedJoinPoint.setTargetMethod(invokeMethod)
        return proceedJoinPoint
    }

    private fun setOnInvokeListener(proceedJoinPoint : ProceedJoinPoint,iterator:MutableIterator<PointCutAnnotation>,returnValue:Array<Any?>){
        proceedJoinPoint.setOnInvokeListener(object :OnInvokeListener{
            override fun onInvoke(): Any? {
                if (iterator.hasNext()) {
                    val nextCutAnnotation = iterator.next()
                    iterator.remove()
                    proceedJoinPoint.setHasNext(iterator.hasNext())
                    val value = if (nextCutAnnotation.basePointCut != null) {
                        nextCutAnnotation.basePointCut!!.invoke(
                            proceedJoinPoint,
                            nextCutAnnotation.annotation!!
                        )
                    } else {
                        nextCutAnnotation.matchClassMethod!!.invoke(
                            proceedJoinPoint,
                            proceedJoinPoint.targetMethod.name
                        )
                    }
                    returnValue[0] = value
                    return value
                } else {
                    return returnValue[0]
                }
            }

            override suspend fun onInvokeSuspend(): Any? {
                if (iterator.hasNext()) {
                    val nextCutAnnotation = iterator.next()
                    iterator.remove()
                    proceedJoinPoint.setHasNext(iterator.hasNext())
                    val value = if (nextCutAnnotation.basePointCut != null) {
                        if (nextCutAnnotation.basePointCut is BasePointCutSuspend){
                            (nextCutAnnotation.basePointCut!! as BasePointCutSuspend).invokeSuspend(
                                proceedJoinPoint,
                                nextCutAnnotation.annotation!!
                            )
                        }else{
                            nextCutAnnotation.basePointCut!!.invoke(
                                proceedJoinPoint,
                                nextCutAnnotation.annotation!!
                            )
                        }
                    } else {
                        if (nextCutAnnotation.matchClassMethod is MatchClassMethodSuspend){
                            (nextCutAnnotation.matchClassMethod!! as MatchClassMethodSuspend).invokeSuspend(
                                proceedJoinPoint,
                                proceedJoinPoint.targetMethod.name
                            )
                        }else{
                            nextCutAnnotation.matchClassMethod!!.invoke(
                                proceedJoinPoint,
                                proceedJoinPoint.targetMethod.name
                            )
                        }

                    }
                    returnValue[0] = value
                    return value
                } else {
                    return returnValue[0]
                }
            }

        })
    }

    private fun readyPointCutAnnotation(proceedJoinPoint : ProceedJoinPoint):MutableList<PointCutAnnotation>{
        val annotations = originalMethod.annotations
        val basePointCuts: MutableList<PointCutAnnotation> = ArrayList()
        for (annotation in annotations) {
            val annotationName: String = annotation.annotationClass.qualifiedName!!
            if (getCutClassCreator(annotationName) != null) {
                val basePointCut: BasePointCut<Annotation>? = getBasePointCut(
                    proceedJoinPoint, annotationName,
                    targetClassName!!,
                    methodKey!!
                )
                if (basePointCut != null) {
                    val pointCutAnnotation = PointCutAnnotation(annotation, basePointCut)
                    basePointCuts.add(pointCutAnnotation)
                }
            }
        }
        if (cutMatchClassName != null && getMatchClassCreator(cutMatchClassName!!) != null) {
            val matchClassMethod = getMatchClassMethod(
                proceedJoinPoint,
                cutMatchClassName!!, targetClassName!!, methodKey!!
            )
            val pointCutAnnotation = PointCutAnnotation(matchClassMethod)
            basePointCuts.add(pointCutAnnotation)
        }
        return basePointCuts
    }

    fun joinPointExecute(zero: Int): Object? {
        val proceedJoinPoint = getProceedJoinPoint()
        val returnValue = arrayOfNulls<Any>(1)
        val basePointCuts: MutableList<PointCutAnnotation> = readyPointCutAnnotation(proceedJoinPoint)
        val iterator = basePointCuts.iterator()
        if (basePointCuts.size > 1) {
            setOnInvokeListener(proceedJoinPoint, iterator, returnValue)
        }
        proceedJoinPoint.setHasNext(basePointCuts.size > 1)
        val cutAnnotation = iterator.next()
        iterator.remove()
        if (cutAnnotation.basePointCut != null) {
            returnValue[0] =
                cutAnnotation.basePointCut!!.invoke(proceedJoinPoint, cutAnnotation.annotation!!)
        } else {
            returnValue[0] = cutAnnotation.matchClassMethod!!.invoke(
                proceedJoinPoint,
                proceedJoinPoint.targetMethod.name
            )
        }
        return if(returnValue[0] != null){
            returnValue[0] as Object
        }else{
            null
        }
    }

    suspend fun joinPointExecute(): Any? {
        val proceedJoinPoint = getProceedJoinPoint()
        val returnValue = arrayOfNulls<Any>(1)
        val basePointCuts: MutableList<PointCutAnnotation> = readyPointCutAnnotation(proceedJoinPoint)
        val iterator = basePointCuts.iterator()
        if (basePointCuts.size > 1) {
            setOnInvokeListener(proceedJoinPoint, iterator, returnValue)
        }
        proceedJoinPoint.setHasNext(basePointCuts.size > 1)
        val cutAnnotation = iterator.next()
        iterator.remove()
        if (cutAnnotation.basePointCut != null) {
            if (cutAnnotation.basePointCut is BasePointCutSuspend){
                returnValue[0] =
                    (cutAnnotation.basePointCut!! as BasePointCutSuspend).invokeSuspend(proceedJoinPoint, cutAnnotation.annotation!!)
            }else{
                returnValue[0] =
                    cutAnnotation.basePointCut!!.invoke(proceedJoinPoint, cutAnnotation.annotation!!)
            }

        } else {
            if (cutAnnotation.matchClassMethod is MatchClassMethodSuspend){
                returnValue[0] = (cutAnnotation.matchClassMethod!! as MatchClassMethodSuspend).invokeSuspend(
                    proceedJoinPoint,
                    proceedJoinPoint.targetMethod.name
                )
            }else{
                returnValue[0] = cutAnnotation.matchClassMethod!!.invoke(
                    proceedJoinPoint,
                    proceedJoinPoint.targetMethod.name
                )
            }

        }
        return returnValue[0]
    }

    internal inner class PointCutAnnotation {
        var annotation: Annotation? = null
        var basePointCut: BasePointCut<Annotation>? = null
        var matchClassMethod: MatchClassMethod? = null

        constructor(annotation: Annotation, basePointCut: BasePointCut<Annotation>) {
            this.annotation = annotation
            this.basePointCut = basePointCut
        }

        constructor(matchClassMethod: MatchClassMethod) {
            this.matchClassMethod = matchClassMethod
        }

        override fun toString(): String {
            return "PointCutAnnotation{" +
                    "annotation=" + (if (annotation != null) annotation?.annotationClass?.qualifiedName
                 else "null") +
                    ", basePointCut=" + (if (basePointCut != null) basePointCut!!.javaClass.name else "null") +
                    ", matchClassMethod=" + (if (matchClassMethod != null) matchClassMethod!!.javaClass.name else "null") +
                    '}'
        }
    }


    fun setCutMatchClassName(cutMatchClassName: String?) {
        this.cutMatchClassName = cutMatchClassName
    }

    fun setArgClasses(argClasses: Array<Class<*>>) {
        mArgClasses = argClasses
    }

    fun setArgs(args: Array<Any?>?) {
        setArgs(args, null)
    }

    fun setArgs(args: Array<Any?>?, invokeMethod: InvokeMethod?) {
        mArgs = args
        this.invokeMethod = invokeMethod
        getTargetMethod()
    }

    private fun getTargetMethod() {
        val stringBuilder = StringBuilder()
        stringBuilder.append("(")
        if (mArgClasses.isNotEmpty()) {
            var index = 0
            for (argClassName in mArgClasses) {
                stringBuilder.append(argClassName.name)
                if (index != mArgClasses.size - 1) {
                    stringBuilder.append(",")
                }
                index++
            }
        }
        stringBuilder.append(")")
        paramsKey = stringBuilder.toString()
        methodKey = originalMethodName + paramsKey
        val key = "$targetClassName-$target-$methodKey"
        var methodMap = getMethodMapCache(key)
        if (methodMap != null) {
            targetMethod = methodMap.targetMethod
            originalMethod = methodMap.originalMethod
            return
        }
        try {
            val classes = mArgClasses
            val tClass = targetClass ?: throw RuntimeException("织入代码异常")
            targetMethod = tClass.getDeclaredMethod(targetMethodName, *classes)
            originalMethod = try {
                tClass.getDeclaredMethod(originalMethodName, *classes)
            } catch (exc: NoSuchMethodException) {
                val realMethodName = getRealMethodName(originalMethodName)
                    ?: throw RuntimeException(exc)
                tClass.getDeclaredMethod(realMethodName, *classes)
            }
            targetMethod.isAccessible = true
            originalMethod.isAccessible = true
            methodMap = MethodMap(originalMethod, targetMethod)
            putMethodMapCache(key, methodMap, target)
        } catch (e: NoSuchMethodException) {
            throw RuntimeException(e)
        }
    }

    private fun getRealMethodName(staticMethodName: String?): String? {
        val stackTrace = Thread.currentThread().stackTrace
        for (element in stackTrace) {
            val methodName = element.methodName
            if (methodName.contains(staticMethodName!!)) {
                return methodName
            }
        }
        return null
    }
}