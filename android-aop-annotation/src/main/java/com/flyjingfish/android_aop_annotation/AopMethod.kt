package com.flyjingfish.android_aop_annotation

import androidx.annotation.RequiresApi
import java.lang.reflect.Parameter
import java.lang.reflect.Type

interface AopMethod {

    /**
     * @return 切点方法名称
     */
    fun getName(): String

    /**
     * @return 切点方法参数所有的变量名
     */
    fun getParameterNames(): Array<String>

    /**
     * 如果切点函数是 suspend 函数并且返回类型是基本数据类型，会自动转化为包装类型
     *
     * @return 返回值类型，抹除了泛型信息
     */
    fun getReturnType(): Class<*>

    /**
     * 如果切点函数是 suspend 函数并且返回类型是基本数据类型，会自动转化为包装类型
     *
     * @return 返回值类型，包含泛型信息
     */
    fun getGenericReturnType(): Type

    /**
     * @return 返回方法所在的类 class 对象
     */
    fun getDeclaringClass(): Class<*>

    /**
     * @return 方法参数的所有类型信息，抹除了泛型信息
     */
    fun getParameterTypes(): Array<Class<*>>

    /**
     * @return 方法参数的所有类型信息，包含泛型信息
     */
    fun getGenericParameterTypes(): Array<Type>

    /**
     * @return 返回一个整数，该整数是描述字段、方法或构造函数的修饰符的位掩码
     */
    fun getModifiers(): Int

    /**
     * @return 返回此方法上的所有注解的数组
     */
    fun getAnnotations(): Array<Annotation>

    /**
     *
     * @param annotationClass 指定类型的注解的 class 对象
     * @return 返回此方法上指定类型的注解
     * @param <T> 具体泛型信息
    </T> */
    fun <T : Annotation> getAnnotation(annotationClass: Class<T>): T

    /**
     * @return 返回方法的参数信息的数组
     */
    @RequiresApi(api = 26)
    fun getParameters(): Array<Parameter>

    /**
     * 返回的长度和参数个数一样，如果参数上没有注解，则对应下标位置的数组长度为0
     *
     * @return 返回方法参数上注解
     */
    fun getParameterAnnotations(): Array<Array<Annotation>>
}