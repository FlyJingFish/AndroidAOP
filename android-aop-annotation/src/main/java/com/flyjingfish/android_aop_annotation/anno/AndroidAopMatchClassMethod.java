package com.flyjingfish.android_aop_annotation.anno;

import static java.lang.annotation.RetentionPolicy.SOURCE;

import com.flyjingfish.android_aop_annotation.enums.MatchType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 定义匹配类及方法切面的注解，使用这个注解的类需要实现 MatchClassMethod 接口，并且这个类也是处理切面的类
 * wiki 文档相关说明
 */
@Target(ElementType.TYPE)
@Retention(SOURCE)
public @interface AndroidAopMatchClassMethod {
    /**
     * @return 目标类名（包含包名）
     */
    String targetClassName();
    /**
     * @return 目标类中的方法名数组
     */
    String[] methodName();

    /**
     *
     * @return 返回匹配类型 {@link MatchType}
     */
    MatchType type() default MatchType.EXTENDS;
}
