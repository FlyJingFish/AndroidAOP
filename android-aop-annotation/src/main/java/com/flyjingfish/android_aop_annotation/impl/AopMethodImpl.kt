package com.flyjingfish.android_aop_annotation.impl

import androidx.annotation.RequiresApi
import com.flyjingfish.android_aop_annotation.AopMethod
import java.lang.reflect.Method
import java.lang.reflect.Parameter
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

internal class AopMethodImpl(
    private val targetMethod: Method,
    private val isSuspend: Boolean,
    private val mParamNames: Array<String>,
    private val mParamClasses: Array<Class<*>>,
    private val mReturnType: Class<*>,
    private val mLambda: Boolean
) : AopMethod {

    override val name: String
        get() = targetMethod.name

    override val parameterNames: Array<String>
        get() {
            if (isSuspend && mParamNames.isNotEmpty()) {
                return mParamNames.copyOfRange(0, mParamNames.size - 1)
            }
            return mParamNames
        }

    override val returnType: Class<*>
        get() = mReturnType

    override val genericReturnType: Type
        get() {
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

    override val declaringClass: Class<*>
        get() = targetMethod.declaringClass

    override val parameterTypes: Array<Class<*>>
        get() {
            if (isSuspend) {
                return mParamClasses.copyOfRange(0, mParamClasses.size - 1)
            }
            return mParamClasses
        }

    override val genericParameterTypes: Array<Type>
        get() {
            val types = targetMethod.genericParameterTypes
            if (isSuspend) {
                return types.copyOfRange(0, types.size - 1)
            }
            return types
        }

    override val modifiers: Int
        get() = targetMethod.modifiers

    override val annotations: Array<Annotation>
        get() = targetMethod.annotations


    override fun <T : Annotation> getAnnotation(annotationClass: Class<T>): T? {
        return targetMethod.getAnnotation(annotationClass)
    }

    override val parameters: Array<Parameter>
        @RequiresApi(api = 26)
        get() {
            val parameters = targetMethod.parameters
            if (isSuspend && parameters.isNotEmpty()) {
                return parameters.copyOfRange(0, parameters.size - 1)
            }
            return parameters
        }

    override val parameterAnnotations: Array<Array<Annotation>>
        get() {
            val parameterAnnotations = targetMethod.parameterAnnotations
            if (isSuspend && parameterAnnotations.isNotEmpty()) {
                return parameterAnnotations.copyOfRange(0, parameterAnnotations.size - 1)
            }
            return parameterAnnotations
        }
    override val isLambda: Boolean
        get() = mLambda

}