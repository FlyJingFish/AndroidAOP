package com.flyjingfish.android_aop_annotation.impl

import androidx.annotation.RequiresApi
import com.flyjingfish.android_aop_annotation.AopMethod
import java.lang.reflect.Method
import java.lang.reflect.Parameter
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

internal class AopMethodImpl(
     private val targetMethod: Method,
     private val isSuspend:Boolean,
     private val suspendContinuation: Any?,
     private val mParamNames: Array<String>,
     private val mParamClasses: Array<Class<*>>,
     private val mReturnType: Class<*>,
): AopMethod {

    override fun getName(): String? {
        return targetMethod.name
    }

    override fun getParameterNames(): Array<String> {
        if (isSuspend && mParamNames.isNotEmpty()) {
            return mParamNames.copyOfRange(0,mParamNames.size - 1)
        }
        return mParamNames
    }

    override fun getReturnType(): Class<*> {
        return mReturnType
    }

    override fun getGenericReturnType(): Type {
        if (isSuspend) {
            val types = targetMethod.genericParameterTypes
            val types1 = types[types.size - 1]
            if (types1 is ParameterizedType) {
                val realTypes = types1.actualTypeArguments
                if (realTypes.isNotEmpty()) {
                    val continuationType = realTypes[0]
                    try {
                        val field = continuationType.javaClass.getDeclaredField("superBound")
                        field.isAccessible = true
                        val superBoundObj = field[continuationType]
                        val typesField = superBoundObj.javaClass.getDeclaredField("types")
                        typesField.isAccessible = true
                        val typesList = typesField[superBoundObj] as List<Type>
                        return typesList[0]
                    } catch (e: Throwable) {
                    }
                }
            }
        }
        return targetMethod.genericReturnType
    }

    override fun getDeclaringClass(): Class<*>? {
        return targetMethod.declaringClass
    }

    override fun getParameterTypes(): Array<Class<*>>? {
        if (isSuspend) {
            return mParamClasses.copyOfRange(0,mParamClasses.size - 1)
        }
        return mParamClasses
    }

    override fun getGenericParameterTypes(): Array<Type>? {
        val types = targetMethod.genericParameterTypes
        if (isSuspend) {
            return types.copyOfRange(0,types.size - 1)
        }
        return types
    }

    override fun getModifiers(): Int {
        return targetMethod.modifiers
    }

    override fun getAnnotations(): Array<Annotation> {
        return targetMethod.annotations
    }

    override fun <T : Annotation?> getAnnotation(annotationClass: Class<T>): T {
        return targetMethod.getAnnotation(annotationClass)
    }

    @RequiresApi(api = 26)
    override fun getParameters(): Array<Parameter?> {
        val parameters = targetMethod.parameters
        if (isSuspend && parameters.isNotEmpty()) {
            return parameters.copyOfRange(0,parameters.size - 1)
        }
        return parameters
    }

    override fun getParameterAnnotations(): Array<Array<Annotation>> {
        val parameterAnnotations = targetMethod.parameterAnnotations
        if (isSuspend && parameterAnnotations.isNotEmpty()) {
            return parameterAnnotations.copyOfRange(0,parameterAnnotations.size - 1)
        }
        return parameterAnnotations
    }
}