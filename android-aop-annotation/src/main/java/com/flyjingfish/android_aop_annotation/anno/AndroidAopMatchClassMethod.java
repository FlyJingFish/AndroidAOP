package com.flyjingfish.android_aop_annotation.anno;

import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(SOURCE)
public @interface AndroidAopMatchClassMethod {
    String targetClassName();

    String[] methodName();
}
