package com.flyjingfish.android_aop_annotation.enums;

import com.flyjingfish.android_aop_annotation.anno.AndroidAopMatchClassMethod;

public enum MatchType {
    /**
     * 表示匹配的是继承自 {@link AndroidAopMatchClassMethod#targetClassName()} 设置的类
     */
    EXTENDS,
    /**
     * 表示匹配的是 {@link AndroidAopMatchClassMethod#targetClassName()} 设置的类自身
     */
    SELF
}
