package com.flyjingfish.android_aop_annotation;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class ProceedJoinPoint {
    public Object[] args;
    public Object target;
    public Class<?> targetClass;
    private Method targetMethod;
    private Method originalMethod;
    private AopMethod targetAopMethod;
    private OnInvokeListener onInvokeListener;
    private boolean hasNext;
    public Object proceed(){
        return proceed(args);
    }
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

    public Object getTarget() {
        return target;
    }

    public Class<?> getTargetClass() {
        return targetClass;
    }

    void setTargetClass(Class<?> targetClass) {
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
