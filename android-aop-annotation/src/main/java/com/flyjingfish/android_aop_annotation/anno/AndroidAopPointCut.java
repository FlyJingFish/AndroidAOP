package com.flyjingfish.android_aop_annotation.anno;


import static java.lang.annotation.RetentionPolicy.SOURCE;


import com.flyjingfish.android_aop_annotation.base.BasePointCut;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;


/**
 * 定义注解切面的注解，使用这个注解的类需要是注解类
 */
@Target({ElementType.ANNOTATION_TYPE})
@Retention(SOURCE)
public @interface AndroidAopPointCut {
    /**
     *
     * @return 处理切面的类
     */
    Class<? extends BasePointCut<? extends Annotation>> value();
}
