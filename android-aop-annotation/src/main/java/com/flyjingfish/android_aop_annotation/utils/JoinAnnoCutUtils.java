package com.flyjingfish.android_aop_annotation.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

final class JoinAnnoCutUtils {
    private static final Map<String,String> mAnnoCutMap = new ConcurrentHashMap<>();
    public static void register(String mapValue){
        System.out.println("mapValue="+mapValue);
        String[] str = mapValue.split("-");
        mAnnoCutMap.put(str[0],str[1]);
    }

    public static String getCutClassName(String className){
        return mAnnoCutMap.get("@"+className);
    }

    public static boolean contains(String className){
        return mAnnoCutMap.containsKey("@"+className);
    }

    public static boolean isInit(){
        return mAnnoCutMap.size() > 0;
    }
}
