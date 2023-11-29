package com.flyjingfish.android_aop_annotation.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AndroidAopMatch {
    String baseClassName();
    String methodNames();
    String pointCutClassName();
    String matchType();
}
