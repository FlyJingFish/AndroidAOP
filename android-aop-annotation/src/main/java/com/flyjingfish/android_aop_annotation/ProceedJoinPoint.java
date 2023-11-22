package com.flyjingfish.android_aop_annotation;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ProceedJoinPoint {
    public Object[] args;
    public Object target;
    private Method targetMethod;
    private Method originalMethod;
    private AopMethod targetAopMethod;
    public Object proceed() throws InvocationTargetException, IllegalAccessException {
        return proceed(args);
    }
    public Object proceed(Object[] args) throws InvocationTargetException, IllegalAccessException {
        return targetMethod.invoke(target,args);
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
