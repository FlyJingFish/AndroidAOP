package com.flyjingfish.android_aop_annotation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JoinAnnoCutUtils {
    private static final List<String> list = new ArrayList<>();
    private static final Map<String,String> mAnnoCutMap = new HashMap<>();
    public static void register(String mapValue){
        System.out.println("mapValue="+mapValue);
        list.add(mapValue);
        String[] str = mapValue.split("-");
        mAnnoCutMap.put(str[0],str[1]);
    }

    public static String getCutClassName(String className){
        return mAnnoCutMap.get("@"+className);
    }

    public static boolean contains(String className){
        return mAnnoCutMap.containsKey("@"+className);
    }
}
