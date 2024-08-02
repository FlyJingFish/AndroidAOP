package com.flyjingfish.android_aop_annotation;


import com.flyjingfish.android_aop_annotation.base.BasePointCut;
import com.flyjingfish.android_aop_annotation.base.BasePointCutSuspend;
import com.flyjingfish.android_aop_annotation.base.MatchClassMethod;
import com.flyjingfish.android_aop_annotation.base.MatchClassMethodSuspend;
import com.flyjingfish.android_aop_annotation.base.OnBaseSuspendReturnListener;
import com.flyjingfish.android_aop_annotation.impl.AopMethodImpl;
import com.flyjingfish.android_aop_annotation.impl.JoinPoint;
import com.flyjingfish.android_aop_annotation.impl.ProceedReturnImpl;
import com.flyjingfish.android_aop_annotation.utils.AndroidAopBeanUtils;
import com.flyjingfish.android_aop_annotation.utils.InvokeMethod;
import com.flyjingfish.android_aop_annotation.utils.MethodMap;
import com.flyjingfish.android_aop_annotation.utils.Utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import kotlin.coroutines.Continuation;

public final class AndroidAopJoinPoint {
    private final Object target;
    private final Class<?> targetClass;
    private Object[] mArgs;
    private Class<?>[] mArgClasses;
    private Class<?> mReturnClass;
    private String[] mParamNames;
    private final String targetMethodName;
    private final String originalMethodName;
    private Method targetMethod;
    private Method originalMethod;
    private String[] cutMatchClassNames;
    private String paramsKey;
    private String methodKey;
    private final String targetClassName;
    private InvokeMethod invokeMethod;

    public AndroidAopJoinPoint(Class<?> clazz, Object target, String originalMethodName, String targetMethodName) {
        this.targetClassName = clazz.getName();
        this.target = target;
        this.originalMethodName = originalMethodName;
        this.targetMethodName = targetMethodName;
        this.targetClass = clazz;

    }


//    public void setCutMatchClassName(String cutMatchClassName) {
//        this.cutMatchClassName = cutMatchClassName;
//    }
    public void setCutMatchClassNames(String[] cutMatchClassNames) {
        this.cutMatchClassNames = cutMatchClassNames;
    }

    public void setArgClasses(Class[] argClasses) {
        this.mArgClasses = argClasses;
    }

    public void setParamNames(String[] paramNames) {
        this.mParamNames = paramNames;
    }

    public void setReturnClass(Class returnClass) {
        this.mReturnClass = returnClass;
    }

    public Object joinPointReturnExecute(Class returnTypeClassName) {

        ProceedReturnImpl proceedReturn = new ProceedReturnImpl(targetClass, mArgs,target);
        proceedReturn.setReturnType$android_aop_annotation(returnTypeClassName);
        proceedReturn.setOriginalMethod$android_aop_annotation(originalMethod);
        proceedReturn.setTargetMethod$android_aop_annotation(targetMethod);
        proceedReturn.setTargetMethod$android_aop_annotation(invokeMethod);
        Object[] returnValue = new Object[1];
        Object startSuspend = Utils.INSTANCE.getStartSuspendObj(target);

        final List<OnBaseSuspendReturnListener> basePointCuts = AndroidAopBeanUtils.INSTANCE.getSuspendReturnListeners(startSuspend);
        AndroidAopBeanUtils.INSTANCE.removeReturnListener(startSuspend);

        if (basePointCuts != null && basePointCuts.size() > 0){
            Iterator<OnBaseSuspendReturnListener> iterator = basePointCuts.iterator();
            if (basePointCuts.size() > 1) {
                proceedReturn.setOnInvokeListener$android_aop_annotation(() -> {
                    if (iterator.hasNext()) {
                        OnBaseSuspendReturnListener listener = iterator.next();
                        iterator.remove();
                        proceedReturn.setHasNext$android_aop_annotation(iterator.hasNext());
                        Object value = Utils.INSTANCE.invokeReturn(proceedReturn,listener);
                        value = Conversions.return2Type(proceedReturn.getReturnType(), value);
                        returnValue[0] = value;
                        return value;
                    }else {
                        return returnValue[0];
                    }
                });
            }

            proceedReturn.setHasNext$android_aop_annotation(basePointCuts.size() > 1);
            OnBaseSuspendReturnListener listener = iterator.next();
            iterator.remove();

            returnValue[0] = Utils.INSTANCE.invokeReturn(proceedReturn,listener);
            for (OnBaseSuspendReturnListener onSuspendReturnListener : basePointCuts) {
                AndroidAopBeanUtils.INSTANCE.removeIgnoreOther(onSuspendReturnListener);
            }
            returnValue[0] = Conversions.return2Type(proceedReturn.getReturnType(), returnValue[0]);
        }else {
            returnValue[0] = proceedReturn.proceed();
        }
        return returnValue[0];
    }

    public Object joinPointExecute(Continuation continuation) {
        boolean isSuspend = continuation != null;

        ProceedJoinPoint proceedJoinPoint;
        AopMethod aopMethod = new AopMethodImpl(originalMethod,isSuspend,continuation,mParamNames,mArgClasses,mReturnClass);
        if (isSuspend){
            proceedJoinPoint = JoinPoint.INSTANCE.getJoinPointSuspend(targetClass, mArgs,target,true,targetMethod,invokeMethod,aopMethod);
        }else {
            proceedJoinPoint = JoinPoint.INSTANCE.getJoinPoint(targetClass, mArgs,target,false,targetMethod,invokeMethod,aopMethod);
        }

        Annotation[] annotations = originalMethod.getAnnotations();
        Object[] returnValue = new Object[1];

        final List<PointCutAnnotation> basePointCuts = new ArrayList<>();

        for (Annotation annotation : annotations) {
            String annotationName = annotation.annotationType().getName();
            if (AndroidAopBeanUtils.INSTANCE.getCutClassCreator(annotationName) != null) {
                BasePointCut<Annotation> basePointCut = AndroidAopBeanUtils.INSTANCE.getBasePointCut(proceedJoinPoint, annotationName,targetClassName,methodKey);
                if (basePointCut != null) {
                    PointCutAnnotation pointCutAnnotation = new PointCutAnnotation(annotation, basePointCut);
                    basePointCuts.add(pointCutAnnotation);
                }
            }
        }

        if (cutMatchClassNames != null) {
            for (String cutMatchClassName : cutMatchClassNames) {
                if (AndroidAopBeanUtils.INSTANCE.getMatchClassCreator(cutMatchClassName) != null){
                    MatchClassMethod matchClassMethod = AndroidAopBeanUtils.INSTANCE.getMatchClassMethod(proceedJoinPoint, cutMatchClassName,targetClassName,methodKey);
                    PointCutAnnotation pointCutAnnotation = new PointCutAnnotation(matchClassMethod);
                    basePointCuts.add(pointCutAnnotation);
                }
            }
        }
        Iterator<PointCutAnnotation> iterator = basePointCuts.iterator();


        if (basePointCuts.size() > 1) {
            JoinPoint.INSTANCE.setOnInvokeListener(proceedJoinPoint,() -> {
                if (iterator.hasNext()) {
                    PointCutAnnotation nextCutAnnotation = iterator.next();
                    iterator.remove();
                    JoinPoint.INSTANCE.setHasNext(proceedJoinPoint,iterator.hasNext());
                    Object value;
                    if (nextCutAnnotation.basePointCut != null) {
                        if (isSuspend){
                            if (nextCutAnnotation.basePointCut instanceof BasePointCutSuspend){
                                value = ((BasePointCutSuspend<Annotation>) nextCutAnnotation.basePointCut).invokeSuspend((ProceedJoinPointSuspend) proceedJoinPoint, nextCutAnnotation.annotation,continuation);
                            }else {
                                try {
                                    value = nextCutAnnotation.basePointCut.invoke(proceedJoinPoint, nextCutAnnotation.annotation);
                                } catch (ClassCastException e) {
                                    String message = e.getMessage();
                                    if (message == null || !message.contains("kotlin.coroutines.intrinsics.CoroutineSingletons")){
                                        throw new RuntimeException(e);
                                    }else {
                                        throw new RuntimeException("协程函数的切面不可修改返回值，请使用 BasePointCutSuspend");
//                                        value = proceedJoinPoint.getMethodReturnValue();
                                    }
                                }
                            }
                        }else {
                            value = nextCutAnnotation.basePointCut.invoke(proceedJoinPoint, nextCutAnnotation.annotation);
                        }
                    } else {
                        if (isSuspend){
                            if (nextCutAnnotation.matchClassMethod instanceof MatchClassMethodSuspend){
                                value = ((MatchClassMethodSuspend) nextCutAnnotation.matchClassMethod).invokeSuspend((ProceedJoinPointSuspend) proceedJoinPoint, proceedJoinPoint.getTargetMethod().getName(),continuation);
                            }else {
                                try {
                                    value = nextCutAnnotation.matchClassMethod.invoke(proceedJoinPoint, proceedJoinPoint.getTargetMethod().getName());
                                } catch (ClassCastException e) {
                                    String message = e.getMessage();
                                    if (message == null || !message.contains("kotlin.coroutines.intrinsics.CoroutineSingletons")){
                                        throw new RuntimeException(e);
                                    }else {
                                        throw new RuntimeException("协程函数的切面不可修改返回值，请使用 MatchClassMethodSuspend");
//                                        value = proceedJoinPoint.getMethodReturnValue();
                                    }
                                }
                            }
                        }else {
                            value = nextCutAnnotation.matchClassMethod.invoke(proceedJoinPoint, proceedJoinPoint.getTargetMethod().getName());
                        }
                    }
                    returnValue[0] = value;
                    return value;
                }else {
                    return returnValue[0];
                }
            });
        }

        JoinPoint.INSTANCE.setHasNext(proceedJoinPoint,basePointCuts.size() > 1);
        PointCutAnnotation cutAnnotation = iterator.next();
        iterator.remove();
        if (cutAnnotation.basePointCut != null) {
            if (isSuspend){
                if (cutAnnotation.basePointCut instanceof BasePointCutSuspend){
                    returnValue[0] = ((BasePointCutSuspend<Annotation>) cutAnnotation.basePointCut).invokeSuspend((ProceedJoinPointSuspend) proceedJoinPoint, cutAnnotation.annotation,continuation);
                }else {
                    try {
                        returnValue[0] = cutAnnotation.basePointCut.invoke(proceedJoinPoint, cutAnnotation.annotation);
                    } catch (ClassCastException e) {
                        String message = e.getMessage();
                        if (message == null || !message.contains("kotlin.coroutines.intrinsics.CoroutineSingletons")){
                            throw new RuntimeException(e);
                        }else {
                            throw new RuntimeException("协程函数的切面不可修改返回值，请使用 BasePointCutSuspend");
//                            returnValue[0] = proceedJoinPoint.getMethodReturnValue();
                        }
                    }
                }
            }else {
                returnValue[0] = cutAnnotation.basePointCut.invoke(proceedJoinPoint, cutAnnotation.annotation);
            }
        } else {
            if (isSuspend){
                if (cutAnnotation.matchClassMethod instanceof MatchClassMethodSuspend){
                    returnValue[0] = ((MatchClassMethodSuspend) cutAnnotation.matchClassMethod).invokeSuspend((ProceedJoinPointSuspend) proceedJoinPoint, proceedJoinPoint.getTargetMethod().getName(),continuation);
                }else {
                    try {
                        returnValue[0] = cutAnnotation.matchClassMethod.invoke(proceedJoinPoint, proceedJoinPoint.getTargetMethod().getName());
                    } catch (ClassCastException e) {
                        String message = e.getMessage();
                        if (message == null || !message.contains("kotlin.coroutines.intrinsics.CoroutineSingletons")){
                            throw new RuntimeException(e);
                        }else {
                            throw new RuntimeException("协程函数的切面不可修改返回值，请使用 MatchClassMethodSuspend");
//                            returnValue[0] = proceedJoinPoint.getMethodReturnValue();
                        }
                    }
                }
            }else {
                returnValue[0] = cutAnnotation.matchClassMethod.invoke(proceedJoinPoint, proceedJoinPoint.getTargetMethod().getName());
            }
        }
        return returnValue[0];
    }

    static class PointCutAnnotation {
        Annotation annotation;
        BasePointCut<Annotation> basePointCut;
        MatchClassMethod matchClassMethod;

        public PointCutAnnotation(Annotation annotation, BasePointCut<Annotation> basePointCut) {
            this.annotation = annotation;
            this.basePointCut = basePointCut;
        }

        public PointCutAnnotation(MatchClassMethod matchClassMethod) {
            this.matchClassMethod = matchClassMethod;
        }

        @Override
        public String toString() {
            return "PointCutAnnotation{" +
                    "annotation=" + (annotation != null ? annotation.annotationType().getName() : "null") +
                    ", basePointCut=" + (basePointCut != null ? basePointCut.getClass().getName() : "null") +
                    ", matchClassMethod=" + (matchClassMethod != null ? matchClassMethod.getClass().getName() : "null") +
                    '}';
        }
    }

    public void setArgs(Object[] args) {
       setArgs(args,null);
    }

    public void setArgs(Object[] args, InvokeMethod invokeMethod) {
        this.mArgs = args;
        this.invokeMethod = invokeMethod;
        getTargetMethod();
    }

    private void getTargetMethod(){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("(");
        if (mArgClasses != null && mArgClasses.length > 0){
            int index = 0;
            for (Class<?> argClassName : mArgClasses) {
                stringBuilder.append(argClassName.getName());
                if (index != mArgClasses.length - 1){
                    stringBuilder.append(",");
                }
                index++;
            }
        }
        stringBuilder.append(")");
        paramsKey = stringBuilder.toString();
        methodKey = originalMethodName + paramsKey;

        String key = targetClassName +"-" + target + "-" + methodKey;
        MethodMap methodMap = AndroidAopBeanUtils.INSTANCE.getMethodMapCache(key);
        if (methodMap != null){
            targetMethod = methodMap.getTargetMethod();
            originalMethod = methodMap.getOriginalMethod();
            return;
        }
        try {
            Class<?>[] classes = mArgClasses;
            if (classes == null) {
                classes = new Class<?>[0];
            }
            Class<?> tClass = targetClass;
            if (tClass == null) {
                throw new RuntimeException("织入代码异常");
            }
            targetMethod = tClass.getDeclaredMethod(targetMethodName, classes);
            try {
                originalMethod = tClass.getDeclaredMethod(originalMethodName, classes);
            } catch (NoSuchMethodException exc) {
                String realMethodName = getRealMethodName(originalMethodName);
                if (realMethodName == null){
                    throw new RuntimeException(exc);
                }
                originalMethod = tClass.getDeclaredMethod(realMethodName, classes);
            }
            targetMethod.setAccessible(true);
            originalMethod.setAccessible(true);
            methodMap = new MethodMap(originalMethod,targetMethod);
            AndroidAopBeanUtils.INSTANCE.putMethodMapCache(key,methodMap,target);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getRealMethodName(String staticMethodName) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (StackTraceElement element : stackTrace) {
            String methodName = element.getMethodName();
            if (methodName.contains(staticMethodName)){
                return methodName;
            }
        }
        return null;
    }

}
