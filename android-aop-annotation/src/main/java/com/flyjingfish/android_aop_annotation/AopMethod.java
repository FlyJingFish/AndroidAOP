package com.flyjingfish.android_aop_annotation;

import androidx.annotation.RequiresApi;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;

/**
 * 此类持有执行方法的反射信息，且进行过缓存了，可放心使用
 */
public interface AopMethod {

    /**
     * @return 切点方法名称
     */
    String getName();

    /**
     * @return 切点方法参数所有的变量名
     */
    String[] getParameterNames();

    /**
     * 如果切点函数是 suspend 函数并且返回类型是基本数据类型，会自动转化为包装类型
     *
     * @return 返回值类型，抹除了泛型信息
     */
    Class<?> getReturnType();

    /**
     * 如果切点函数是 suspend 函数并且返回类型是基本数据类型，会自动转化为包装类型
     *
     * @return 返回值类型，包含泛型信息
     */
    Type getGenericReturnType();

    /**
     * @return 返回方法所在的类 class 对象
     */
    Class<?> getDeclaringClass();

    /**
     * @return 方法参数的所有类型信息，抹除了泛型信息
     */
    Class<?>[] getParameterTypes();

    /**
     * @return 方法参数的所有类型信息，包含泛型信息
     */
    Type[] getGenericParameterTypes();

    /**
     * @return 返回一个整数，该整数是描述字段、方法或构造函数的修饰符的位掩码
     */
    int getModifiers();

    /**
     * @return 返回此方法上的所有注解的数组
     */
    Annotation[] getAnnotations();

    /**
     *
     * @param annotationClass 指定类型的注解的 class 对象
     * @return 返回此方法上指定类型的注解
     * @param <T> 具体泛型信息
     */
    <T extends Annotation> T getAnnotation(Class<T> annotationClass);

    /**
     * @return 返回方法的参数信息的数组
     */
    @RequiresApi(api = 26)
    Parameter[] getParameters();

    /**
     * 返回的长度和参数个数一样，如果参数上没有注解，则对应下标位置的数组长度为0
     *
     * @return 返回方法参数上注解
     */
    Annotation[][] getParameterAnnotations();
}
