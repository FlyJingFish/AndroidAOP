package com.flyjingfish.android_aop_annotation.enums;

import com.flyjingfish.android_aop_annotation.anno.AndroidAopMatchClassMethod;

public enum MatchType {
    /**
     * 表示匹配的是继承自 {@link AndroidAopMatchClassMethod#targetClassName()} 设置的类<br>
     * 包含 {@link MatchType#DIRECT_EXTENDS},{@link MatchType#LEAF_EXTENDS}
     */
    EXTENDS,
    /**
     * 表示匹配的是 {@link AndroidAopMatchClassMethod#targetClassName()} 设置的类自身
     */
    SELF,
    /**
     * 表示的匹配的是 <em><strong>直接继承</strong></em> 自 {@link AndroidAopMatchClassMethod#targetClassName()} 设置的类
     */
    DIRECT_EXTENDS,
    /**
     * 表示的匹配的是 <em><strong>末端继承（就是没有子类了，最终子类）</strong></em> 自 {@link AndroidAopMatchClassMethod#targetClassName()} 设置的类<br>
     * 默认不扫描 kotlin/ 和kotlinx/ 这两个包下的代码
     */
    LEAF_EXTENDS
}
