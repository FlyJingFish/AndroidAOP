package com.flyjingfish.android_aop_annotation.utils;

import com.flyjingfish.android_aop_annotation.base.BasePointCutCreator;
import com.flyjingfish.android_aop_annotation.base.MatchClassMethodCreator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

final class JoinAnnoCutUtils {
    private static final Map<String, BasePointCutCreator> mAnnoCutBeanMap = new ConcurrentHashMap<>();
    private static final Map<String, MatchClassMethodCreator> mAnnoMatchBeanMap = new ConcurrentHashMap<>();
    static {
        registerCreators();
        registerMatchCreators();
    }

    private static void registerMatchCreators() {
    }

    private static void registerCreators(){

    }

    private static void registerCreator(String className,BasePointCutCreator cutCreator){
        mAnnoCutBeanMap.put(className,cutCreator);
    }
    public static BasePointCutCreator getCutClassCreator(String className){
        return mAnnoCutBeanMap.get("@"+className);
    }

    private static void registerMatchCreator(String className,MatchClassMethodCreator cutCreator){
        mAnnoMatchBeanMap.put(className,cutCreator);
    }
    public static MatchClassMethodCreator getMatchClassCreator(String className){
        return mAnnoMatchBeanMap.get(className);
    }

}
