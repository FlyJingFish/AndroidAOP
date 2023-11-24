package com.flyjingfish.android_aop_annotation;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class ProceedJoinPoint {
    public Object[] args;
    public Object target;
    private Method targetMethod;
    private Method originalMethod;
    private AopMethod targetAopMethod;
    public Object proceed(){
        return proceed(args);
    }
    public Object proceed(Object[] args){
        try {
            return targetMethod.invoke(target,args);
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
        targetAopMethod = new AopMethod();
        targetAopMethod.setOriginalMethod(originalMethod);
    }

    public Object getTarget() {
        return target;
    }
}
