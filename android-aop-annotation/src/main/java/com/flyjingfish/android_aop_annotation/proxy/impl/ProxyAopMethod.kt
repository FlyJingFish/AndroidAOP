package com.flyjingfish.android_aop_annotation.proxy.impl

import androidx.annotation.RequiresApi
import com.flyjingfish.android_aop_annotation.AopMethod
import com.flyjingfish.android_aop_annotation.anno.AndroidAopMatchClassMethod
import com.flyjingfish.android_aop_annotation.anno.AndroidAopReplaceClass
import com.flyjingfish.android_aop_annotation.proxy.ProxyType
import java.lang.reflect.Parameter
import java.lang.reflect.Type

/**
 *
 * 代理 [AopMethod]，并使 [AndroidAopReplaceClass] 使用起来类似于 [AndroidAopMatchClassMethod]
 */
internal class ProxyAopMethod(private val aopMethod: AopMethod,val type: ProxyType?) :AopMethod{

    override val name: String
        get() = aopMethod.name
    override val parameterNames: Array<String>
        get() {
            return if (type == ProxyType.METHOD && aopMethod.parameterNames.isNotEmpty()){
                aopMethod.parameterNames.copyOfRange(1,aopMethod.parameterNames.size)
            }else{
                aopMethod.parameterNames
            }
        }
    override val returnType: Class<*>
        get() = aopMethod.returnType
    override val genericReturnType: Type
        get() = aopMethod.genericReturnType
    override val declaringClass: Class<*>
        get() = aopMethod.declaringClass
    override val parameterTypes: Array<Class<*>>
        get() {
            val types = aopMethod.parameterTypes
            return if (type == ProxyType.METHOD && types.isNotEmpty()){
                types.copyOfRange(1,types.size)
            }else{
                types
            }
        }
    override val genericParameterTypes: Array<Type>
        get() {
            val types = aopMethod.genericParameterTypes
            return if (type == ProxyType.METHOD && types.isNotEmpty()){
                types.copyOfRange(1,types.size)
            }else{
                types
            }
        }
    override val modifiers: Int
        get() {
            if (type != null){
                throw IllegalArgumentException("不支持此操作")
            }else{
                return aopMethod.modifiers
            }
        }
    override val annotations: Array<Annotation>
        get() = aopMethod.annotations

    override fun <T : Annotation> getAnnotation(annotationClass: Class<T>): T? {
        return aopMethod.getAnnotation(annotationClass)
    }

    @get:RequiresApi(api = 26)
    override val parameters: Array<Parameter>
        get() {
            val values = aopMethod.parameters
            return if (type == ProxyType.METHOD && values.isNotEmpty()){
                values.copyOfRange(1,values.size)
            }else{
                values
            }
        }
    override val parameterAnnotations: Array<Array<Annotation>>
        get() {
            val values = aopMethod.parameterAnnotations
            return if (type == ProxyType.METHOD && values.isNotEmpty()){
                values.copyOfRange(1,values.size)
            }else{
                values
            }
        }
}