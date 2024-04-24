package com.flyjingfish.android_aop_annotation.utils;

import com.flyjingfish.android_aop_annotation.base.BasePointCutCreator;
import com.flyjingfish.android_aop_annotation.base.MatchClassMethodCreator;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

final class JoinAnnoCutUtils {
    private static final Map<String, BasePointCutCreator> mAnnoCutBeanMap = new ConcurrentHashMap<>();
    private static final Map<String, MatchClassMethodCreator> mAnnoMatchBeanMap = new ConcurrentHashMap<>();
    static {
        registerCreators();
        registerMatchCreators();
        try {
            Class<?> initClass = Class.forName("com.flyjingfish.android_aop_annotation.utils.DebugAndroidAopInit");
            Method method = initClass.getDeclaredMethod("init");
            method.invoke(null);
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException |
                 IllegalAccessException ignored) {
        }
    }

    private static void registerMatchCreators() {
    }

    private static void registerCreators(){

    }

    static void registerCreator(String className,BasePointCutCreator cutCreator){
        mAnnoCutBeanMap.put(className,cutCreator);
    }
    public static BasePointCutCreator getCutClassCreator(String className){
        return mAnnoCutBeanMap.get("@"+className);
    }

    static void registerMatchCreator(String className,MatchClassMethodCreator cutCreator){
        mAnnoMatchBeanMap.put(className,cutCreator);
    }
    public static MatchClassMethodCreator getMatchClassCreator(String className){
        return mAnnoMatchBeanMap.get(className);
    }

}
