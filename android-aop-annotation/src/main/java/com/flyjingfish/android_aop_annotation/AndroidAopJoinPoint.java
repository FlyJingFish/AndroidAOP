package com.flyjingfish.android_aop_annotation;


import com.flyjingfish.android_aop_annotation.base.BasePointCut;
import com.flyjingfish.android_aop_annotation.base.MatchClassMethod;
import com.flyjingfish.android_aop_annotation.utils.AndroidAopBeanUtils;
import com.flyjingfish.android_aop_annotation.utils.JoinAnnoCutUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public final class AndroidAopJoinPoint {
    private Object target;
    private Class<?> targetClass;
    private String targetClassName;
    private Object[] mArgs;
    private String[] mArgClassNames;
    private final String targetMethodName;
    private final String originalMethodName;
    private Method targetMethod;
    private Method originalMethod;
    private String cutMatchClassName;

    public AndroidAopJoinPoint(String targetClassName, Object target, String originalMethodName, String targetMethodName) {
        this.targetClassName = targetClassName;
        try {
            targetClass = Class.forName(targetClassName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
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
        ProceedJoinPoint proceedJoinPoint = new ProceedJoinPoint();
        proceedJoinPoint.target = target;
        proceedJoinPoint.args = mArgs;
        proceedJoinPoint.setOriginalMethod(originalMethod);
        proceedJoinPoint.setTargetMethod(targetMethod);
        proceedJoinPoint.setTargetClass(targetClass);
        Annotation[] annotations = originalMethod.getAnnotations();
        Object returnValue = null;

        if (cutMatchClassName != null) {
            MatchClassMethod matchClassMethod = AndroidAopBeanUtils.INSTANCE.getMatchClassMethod(proceedJoinPoint, cutMatchClassName);
            matchClassMethod.invoke(proceedJoinPoint, proceedJoinPoint.getTargetMethod().getName());
        }

        for (Annotation annotation : annotations) {
            String cutClassName = JoinAnnoCutUtils.getCutClassName(annotation.annotationType().getName());
            if (cutClassName != null) {
                BasePointCut<Annotation> basePointCut = AndroidAopBeanUtils.INSTANCE.getBasePointCut(proceedJoinPoint, cutClassName);
                if (basePointCut != null) {
                    returnValue = basePointCut.invoke(proceedJoinPoint, annotation);
                }
            }
        }

        return returnValue;
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
                    originalMethod = targetClass.getDeclaredMethod(originalMethodName, classes);
                }
            }
            targetMethod.setAccessible(true);
            originalMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }


}
