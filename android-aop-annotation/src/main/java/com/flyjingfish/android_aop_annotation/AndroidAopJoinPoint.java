package com.flyjingfish.android_aop_annotation;


import com.flyjingfish.android_aop_annotation.base.BasePointCut;
import com.flyjingfish.android_aop_annotation.base.MatchClassMethod;
import com.flyjingfish.android_aop_annotation.utils.AndroidAopBeanUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class AndroidAopJoinPoint {
    private final Object target;
    private final Class<?> targetClass;
//    private final String targetClassName;
    private Object[] mArgs;
    private String[] mArgClassNames;
    private final String targetMethodName;
    private final String originalMethodName;
    private Method targetMethod;
    private Method originalMethod;
    private String cutMatchClassName;

    public AndroidAopJoinPoint(String targetClassName, Object target, String originalMethodName, String targetMethodName) {
//        this.targetClassName = targetClassName;
        try {
            targetClass = Class.forName(targetClassName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(targetClassName + "的类名不可被混淆");
        }
        this.target = target;
        this.originalMethodName = originalMethodName;
        this.targetMethodName = targetMethodName;
    }


    public void setCutMatchClassName(String cutMatchClassName) {
        this.cutMatchClassName = cutMatchClassName;
    }

    public void setArgClassNames(String[] argClassNames) {
        this.mArgClassNames = argClassNames;
    }

    public Object joinPointExecute() {
        ProceedJoinPoint proceedJoinPoint = new ProceedJoinPoint(targetClass, mArgs);
        proceedJoinPoint.target = target;
        proceedJoinPoint.setOriginalMethod(originalMethod);
        proceedJoinPoint.setTargetMethod(targetMethod);
        Annotation[] annotations = originalMethod.getAnnotations();
        Object[] returnValue = new Object[1];

        final List<PointCutAnnotation> basePointCuts = new ArrayList<>();

        for (Annotation annotation : annotations) {
            String annotationName = annotation.annotationType().getName();
            String cutClassName = AndroidAopBeanUtils.INSTANCE.getCutClassName(annotationName);
            if (cutClassName != null) {
                BasePointCut<Annotation> basePointCut = AndroidAopBeanUtils.INSTANCE.getBasePointCut(proceedJoinPoint, cutClassName, annotationName);
                if (basePointCut != null) {
                    PointCutAnnotation pointCutAnnotation = new PointCutAnnotation(annotation, basePointCut);
                    basePointCuts.add(pointCutAnnotation);
                }
            }
        }

        if (cutMatchClassName != null) {
            MatchClassMethod matchClassMethod = AndroidAopBeanUtils.INSTANCE.getMatchClassMethod(proceedJoinPoint, cutMatchClassName);
            PointCutAnnotation pointCutAnnotation = new PointCutAnnotation(matchClassMethod);
            basePointCuts.add(pointCutAnnotation);
        }
        Iterator<PointCutAnnotation> iterator = basePointCuts.iterator();


        if (basePointCuts.size() > 1) {
            proceedJoinPoint.setOnInvokeListener(returnValue1 -> {
                if (iterator.hasNext()) {
                    PointCutAnnotation nextCutAnnotation = iterator.next();
                    iterator.remove();
                    proceedJoinPoint.setHasNext(iterator.hasNext());
                    if (nextCutAnnotation.basePointCut != null) {
                        returnValue[0] = nextCutAnnotation.basePointCut.invoke(proceedJoinPoint, nextCutAnnotation.annotation);
                    } else {
                        returnValue[0] = nextCutAnnotation.matchClassMethod.invoke(proceedJoinPoint, proceedJoinPoint.getTargetMethod().getName());
                    }
                }
            });
        }

        proceedJoinPoint.setHasNext(basePointCuts.size() > 1);
        PointCutAnnotation cutAnnotation = iterator.next();
        iterator.remove();
        if (cutAnnotation.basePointCut != null) {
            returnValue[0] = cutAnnotation.basePointCut.invoke(proceedJoinPoint, cutAnnotation.annotation);
        } else {
            returnValue[0] = cutAnnotation.matchClassMethod.invoke(proceedJoinPoint, proceedJoinPoint.getTargetMethod().getName());
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
        this.mArgs = args;
        try {
            Class<?>[] classes;
            if (mArgClassNames != null && mArgClassNames.length > 0) {
                classes = new Class[args.length];
                int index = 0;
                for (String className : mArgClassNames) {
                    try {
                        Class<?> c = Conversions.getClass_(className);
                        classes[index] = c;
                    } catch (ClassNotFoundException ignored) {
                    }

                    index++;
                }
            } else {
                classes = new Class<?>[0];
            }
            Class<?> tClass = null;
            if (target != null) {
                tClass = target.getClass();
            }
            if (tClass == null) {
                tClass = targetClass;
            }
            if (tClass == null) {
                throw new RuntimeException("织入代码异常");
            }
            try {
                targetMethod = tClass.getDeclaredMethod(targetMethodName, classes);
            } catch (NoSuchMethodException e) {
                try {
                    targetMethod = tClass.getMethod(targetMethodName, classes);
                } catch (NoSuchMethodException ex) {
                    targetMethod = targetClass.getDeclaredMethod(targetMethodName, classes);
                }
            }
            try {
                originalMethod = tClass.getDeclaredMethod(originalMethodName, classes);
            } catch (NoSuchMethodException e) {
                try {
                    originalMethod = tClass.getMethod(originalMethodName, classes);
                } catch (NoSuchMethodException ex) {
                    try {
                        originalMethod = targetClass.getDeclaredMethod(originalMethodName, classes);
                    } catch (NoSuchMethodException exc) {
                        String realMethodName = getRealName(originalMethodName);
                        if (realMethodName == null){
                            throw new RuntimeException(exc);
                        }
                        originalMethod = targetClass.getDeclaredMethod(realMethodName, classes);
                    }
                }
            }
            targetMethod.setAccessible(true);
            originalMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getRealName(String staticMethodName) {
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
