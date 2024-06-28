package com.flyjingfish.android_aop_annotation;


import com.flyjingfish.android_aop_annotation.base.BasePointCut;
import com.flyjingfish.android_aop_annotation.base.BasePointCutSuspend;
import com.flyjingfish.android_aop_annotation.base.MatchClassMethod;
import com.flyjingfish.android_aop_annotation.base.MatchClassMethodSuspend;
import com.flyjingfish.android_aop_annotation.utils.AndroidAopBeanUtils;
import com.flyjingfish.android_aop_annotation.utils.InvokeMethod;
import com.flyjingfish.android_aop_annotation.utils.MethodMap;

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
    private final String targetMethodName;
    private final String originalMethodName;
    private Method targetMethod;
    private Method originalMethod;
    private String cutMatchClassName;
    private String paramsKey;
    private String methodKey;
    private final String targetClassName;

    public AndroidAopJoinPoint(Class<?> clazz, Object target, String originalMethodName, String targetMethodName) {
        this.targetClassName = clazz.getName();
        this.target = target;
        this.originalMethodName = originalMethodName;
        this.targetMethodName = targetMethodName;
        this.targetClass = clazz;

    }


    public void setCutMatchClassName(String cutMatchClassName) {
        this.cutMatchClassName = cutMatchClassName;
    }

    public void setArgClasses(Class[] argClasses) {
        this.mArgClasses = argClasses;
    }

    private boolean isSuspend;

    public Object joinPointExecute(Continuation continuation) {
        isSuspend = continuation != null;
        ProceedJoinPoint proceedJoinPoint = new ProceedJoinPoint(targetClass, mArgs,isSuspend);
        proceedJoinPoint.target = target;
        proceedJoinPoint.setOriginalMethod(originalMethod);
        proceedJoinPoint.setTargetMethod(targetMethod);
        proceedJoinPoint.setTargetMethod(invokeMethod);
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

        if (cutMatchClassName != null && AndroidAopBeanUtils.INSTANCE.getMatchClassCreator(cutMatchClassName) != null) {
            MatchClassMethod matchClassMethod = AndroidAopBeanUtils.INSTANCE.getMatchClassMethod(proceedJoinPoint, cutMatchClassName,targetClassName,methodKey);
            PointCutAnnotation pointCutAnnotation = new PointCutAnnotation(matchClassMethod);
            basePointCuts.add(pointCutAnnotation);
        }
        Iterator<PointCutAnnotation> iterator = basePointCuts.iterator();


        if (basePointCuts.size() > 1) {
            proceedJoinPoint.setOnInvokeListener(() -> {
                if (iterator.hasNext()) {
                    PointCutAnnotation nextCutAnnotation = iterator.next();
                    iterator.remove();
                    proceedJoinPoint.setHasNext(iterator.hasNext());
                    Object value;
                    if (nextCutAnnotation.basePointCut != null) {
                        if (isSuspend && nextCutAnnotation.basePointCut instanceof BasePointCutSuspend){
                            value = ((BasePointCutSuspend<Annotation>) nextCutAnnotation.basePointCut).invokeSuspend(proceedJoinPoint, nextCutAnnotation.annotation,continuation);
                        }else {
                            value = nextCutAnnotation.basePointCut.invoke(proceedJoinPoint, nextCutAnnotation.annotation);
                        }
                    } else {
                        if (isSuspend && nextCutAnnotation.matchClassMethod instanceof MatchClassMethodSuspend){
                            value = ((MatchClassMethodSuspend) nextCutAnnotation.matchClassMethod).invokeSuspend(proceedJoinPoint, proceedJoinPoint.getTargetMethod().getName(),continuation);
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

        proceedJoinPoint.setHasNext(basePointCuts.size() > 1);
        PointCutAnnotation cutAnnotation = iterator.next();
        iterator.remove();
        if (cutAnnotation.basePointCut != null) {
            if (isSuspend && cutAnnotation.basePointCut instanceof BasePointCutSuspend){
                returnValue[0] = ((BasePointCutSuspend<Annotation>) cutAnnotation.basePointCut).invokeSuspend(proceedJoinPoint, cutAnnotation.annotation,continuation);
            }else {
                returnValue[0] = cutAnnotation.basePointCut.invoke(proceedJoinPoint, cutAnnotation.annotation);
            }
        } else {
            if (isSuspend && cutAnnotation.matchClassMethod instanceof MatchClassMethodSuspend){
                returnValue[0] = ((MatchClassMethodSuspend) cutAnnotation.matchClassMethod).invokeSuspend(proceedJoinPoint, proceedJoinPoint.getTargetMethod().getName(),continuation);
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
    private InvokeMethod invokeMethod;
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
