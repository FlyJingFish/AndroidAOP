package com.flyjingfish.android_aop_annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * 此类持有执行方法的反射信息，且进行过缓存了，可放心使用
 */
public final class AopMethod {
    private final Method targetMethod;
    private final boolean isSuspend;
    private final Object suspendContinuation;

    private final String[] mParamNames;
    private final Class<?> mReturnType;
    private final Class<?>[] mParamClasses;

    AopMethod(Method targetMethod, boolean isSuspend, Object suspendContinuation, String[] paramNames,Class<?>[] paramClasses,Class<?> returnType) {
        this.targetMethod = targetMethod;
        this.isSuspend = isSuspend;
        this.suspendContinuation = suspendContinuation;
        this.mParamNames = paramNames;
        this.mParamClasses = paramClasses;
        this.mReturnType = returnType;
    }

    public String getName() {
        return targetMethod.getName();
    }

    public String[] getParameterNames() {
        if (isSuspend && mParamNames.length > 0){
            String[] newNames = new String[mParamNames.length - 1];
            System.arraycopy(mParamNames, 0, newNames, 0, newNames.length);
            return newNames;
        }

        return mParamNames;
    }

    public Class<?> getReturnType() {
        if (mReturnType != null){
            return mReturnType;
        }
        return targetMethod.getReturnType();
    }

    public Type getGenericReturnType() {
        return targetMethod.getGenericReturnType();
    }

    public Class<?> getDeclaringClass() {
        return targetMethod.getDeclaringClass();
    }

    public Class<?>[] getParameterTypes() {
        Class<?>[] cls;
        if (mParamClasses != null){
            cls = mParamClasses;
        }else {
            cls = targetMethod.getParameterTypes();
        }
        if (isSuspend){
            Class<?>[] newCls = new Class[cls.length - 1];
            System.arraycopy(cls, 0, newCls, 0, newCls.length);
            return newCls;
        }

        return cls;
    }

    public Type[] getGenericParameterTypes() {
        Type[] types = targetMethod.getGenericParameterTypes();
        if (isSuspend){
            Type[] newTypes = new Class[types.length - 1];
            System.arraycopy(types, 0, newTypes, 0, newTypes.length);
            return newTypes;
        }
        return types;
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
