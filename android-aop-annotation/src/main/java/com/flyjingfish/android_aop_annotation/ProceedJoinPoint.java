package com.flyjingfish.android_aop_annotation;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ProceedJoinPoint {
    public Object[] args;
    public Object target;
    private Method targetMethod;
    private Method originalMethod;
    private AopMethod targetLightMethod;
    public Object proceed() throws InvocationTargetException, IllegalAccessException {
        return proceed(args);
    }
    public Object proceed(Object[] args) throws InvocationTargetException, IllegalAccessException {
        return targetMethod.invoke(target,args);
    }

    public AopMethod getTargetMethod() {
        return targetLightMethod;
    }

    public void setTargetMethod(Method targetMethod) {
        this.targetMethod = targetMethod;
    }

    public void setOriginalMethod(Method originalMethod) {
        this.originalMethod = originalMethod;
        targetLightMethod = new AopMethod();
        targetLightMethod.setOriginalMethod(originalMethod);
    }

    public Object getTarget() {
        return target;
    }
}
