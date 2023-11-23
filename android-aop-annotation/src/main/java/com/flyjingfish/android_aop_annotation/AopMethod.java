package com.flyjingfish.android_aop_annotation;

import java.lang.reflect.Method;

public final class AopMethod {
    private Method originalMethod;
    public String name;
    void setOriginalMethod(Method originalMethod) {
        this.originalMethod = originalMethod;
        this.name = originalMethod.getName();
    }
}
