package com.flyjingfish.android_aop_annotation;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class ProceedJoinPoint {
    @Nullable
    public Object[] args;
    @Nullable
    public Object target;
    @NotNull
    public Class<?> targetClass;
    private Method targetMethod;
    private Method originalMethod;
    private AopMethod targetAopMethod;
    private OnInvokeListener onInvokeListener;
    private boolean hasNext;

    public ProceedJoinPoint(@NotNull Class<?> targetClass) {
        this.targetClass = targetClass;
    }

    @Nullable
    public Object proceed(){
        return proceed(args);
    }
    @Nullable
    public Object proceed(Object... args){
        this.args = args;
        try {
            Object returnValue = null;
            if (!hasNext){
                returnValue = targetMethod.invoke(target,args);
            }
            if (onInvokeListener != null){
                onInvokeListener.onInvoke(returnValue);
            }
            return returnValue;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e.getTargetException());
        }
    }

    @NotNull
    public AopMethod getTargetMethod() {
        return targetAopMethod;
    }

    void setTargetMethod(Method targetMethod) {
        this.targetMethod = targetMethod;
    }

    void setOriginalMethod(Method originalMethod) {
        this.originalMethod = originalMethod;
        targetAopMethod = new AopMethod(originalMethod);
    }

    @Nullable
    public Object getTarget() {
        return target;
    }

    @NotNull
    public Class<?> getTargetClass() {
        return targetClass;
    }

    void setTargetClass(@NotNull Class<?> targetClass) {
        this.targetClass = targetClass;
    }

    interface OnInvokeListener{
        void onInvoke(Object returnValue);
    }

    void setOnInvokeListener(OnInvokeListener onInvokeListener) {
        this.onInvokeListener = onInvokeListener;
    }

    void setHasNext(boolean hasNext) {
        this.hasNext = hasNext;
    }
}
