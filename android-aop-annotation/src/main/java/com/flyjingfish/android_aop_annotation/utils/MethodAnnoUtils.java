package com.flyjingfish.android_aop_annotation.utils;


final class MethodAnnoUtils {
    private static boolean isInit;
    public static void registerMap(){
//        register("");
        isInit = true;
    }

    public static void register(String mapValue){
        JoinAnnoCutUtils.register(mapValue);
    }

    public static boolean isInit() {
        return isInit;
    }
}
