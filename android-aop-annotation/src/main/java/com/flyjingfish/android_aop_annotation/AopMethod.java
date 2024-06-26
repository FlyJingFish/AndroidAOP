package com.flyjingfish.android_aop_annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * 此类持有执行方法的反射信息，且进行过缓存了，可放心使用
 */
public final class AopMethod {
    private final Method targetMethod;

    AopMethod(Method targetMethod) {
        this.targetMethod = targetMethod;
    }

    public String getName() {
        return targetMethod.getName();
    }

    public Class<?> getReturnType() {
        return targetMethod.getReturnType();
    }

    public Type getGenericReturnType() {
        return targetMethod.getGenericReturnType();
    }

    public Class<?> getDeclaringClass() {
        return targetMethod.getDeclaringClass();
    }

    public Class<?>[] getParameterTypes() {
        return targetMethod.getParameterTypes();
    }

    public Type[] getGenericParameterTypes() {
        return targetMethod.getGenericParameterTypes();
    }

    public int getModifiers() {
        return targetMethod.getModifiers();
    }

    public Annotation[] getAnnotations() {
        return targetMethod.getAnnotations();
    }

    public <T extends Annotation> T getAnnotation(Class<T> var1) {
        return targetMethod.getAnnotation(var1);
    }
}
