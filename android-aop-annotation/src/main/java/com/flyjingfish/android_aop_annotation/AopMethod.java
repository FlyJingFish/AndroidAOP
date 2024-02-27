package com.flyjingfish.android_aop_annotation;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

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

}
