package com.flyjingfish.android_aop_processor;

public class CollectMethod {
    /**
     * 需要收集的类名（包含包名）
     */
    String collectClassName;
    /**
     * 执行类名（包含包名）
     */
    String invokeClassName;
    /**
     * 执行方法
     */
    String invokeMethod;
    /**
     * 是否是 class
     */
    String isClazz;
    /**
     * 正则表达式
     */
    String regex;
    /**
     * 收集的类型
     */
    String collectType;

    public CollectMethod(String collectClassName, String invokeClassName, String invokeMethod, String isClazz, String regex, String collectType) {
        this.collectClassName = collectClassName;
        this.invokeClassName = invokeClassName;
        this.invokeMethod = invokeMethod;
        this.isClazz = isClazz;
        this.regex = regex;
        this.collectType = collectType;
    }
}
