package com.flyjingfish.android_aop_core.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.jvm.functions.Function2;
import kotlinx.coroutines.BuildersKt;

public class BuildersKtWithContext {
//    (Lkotlin/coroutines/CoroutineContext;Lkotlin/jvm/functions/Function2;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    public static Object withContext(CoroutineContext context, Function2 function2, Continuation continuation){
        try {
            Method method = continuation.getClass().getMethod("getCompletion");
            method.setAccessible(true);
            Object targetKey = method.invoke(continuation);

            Method method2 = function2.getClass().getMethod("getCompletion");
            method2.setAccessible(true);
            Object targetKey2 = method2.invoke(function2);
            System.out.println("BuildersKtWithContext===CoroutineContext="+context+"==function2="+System.identityHashCode(function2)+"==continuation="+System.identityHashCode(continuation)+"==targetKey="+System.identityHashCode(targetKey));
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }


        return BuildersKt.withContext(context, function2, continuation);
    }
}
