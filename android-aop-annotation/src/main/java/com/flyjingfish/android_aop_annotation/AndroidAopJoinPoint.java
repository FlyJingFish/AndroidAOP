package com.flyjingfish.android_aop_annotation;


import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public final class AndroidAopJoinPoint {
    public Object joinPointExecute(){
        ProceedJoinPoint proceedJoinPoint = new ProceedJoinPoint();
        proceedJoinPoint.target = target;
        proceedJoinPoint.args = mArgs;
        proceedJoinPoint.setOriginalMethod(originalMethod);
        proceedJoinPoint.setTargetMethod(targetMethod);
        proceedJoinPoint.setTargetClass(targetClass);
        Annotation[] annotations = originalMethod.getAnnotations();
        Object returnValue = null;

        if (cutMatchClassName != null){
            MatchClassMethod matchClassMethod = AndroidAopBeanUtils.INSTANCE.getMatchClassMethod(proceedJoinPoint,cutMatchClassName);
            matchClassMethod.invoke(proceedJoinPoint,proceedJoinPoint.getTargetMethod().name);
        }

        for (Annotation annotation : annotations) {
            String cutClassName = JoinAnnoCutUtils.getCutClassName(annotation.annotationType().getName());
            if (cutClassName != null){
                BasePointCut<Annotation> basePointCut = AndroidAopBeanUtils.INSTANCE.getBasePointCut(proceedJoinPoint,cutClassName);
                if (basePointCut != null){
                    returnValue = basePointCut.invoke(proceedJoinPoint,annotation);
                }
            }
        }

        return returnValue;
    }
    private Object target;
    private Class<?> targetClass;
    private String targetClassName;
    private Object[] mArgs;
    private String[] mArgClassNames;
    private String targetMethodName;
    private String originalMethodName;
    private Method targetMethod;
    private Method originalMethod;
    private String cutMatchClassName;

    public AndroidAopJoinPoint(Object target, String originalMethodName, String targetMethodName) {
        this.target = target;
        this.originalMethodName = originalMethodName;
        this.targetMethodName = targetMethodName;
    }
    public AndroidAopJoinPoint(String targetClassName, String originalMethodName, String targetMethodName) {
        this.targetClassName = targetClassName;
        this.originalMethodName = originalMethodName;
        this.targetMethodName = targetMethodName;
    }


    public String getCutMatchClassName() {
        return cutMatchClassName;
    }

    public void setCutMatchClassName(String cutMatchClassName) {
        this.cutMatchClassName = cutMatchClassName;
    }

    public Object[] getArgs() {
        return mArgs;
    }

    public void setArgs(Object[] args) {
        this.mArgs = args;
        try {
            Class<?>[] classes = null;
            if (mArgClassNames != null && mArgClassNames.length > 0){
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
            }else {
                classes = new Class<?>[0];
            }
            Class<?> tClass = null;
            if (target != null) {
                tClass = target.getClass();
            }else if (targetClassName != null){
                try {
                    tClass = Class.forName(targetClassName);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
            if (tClass == null){
                throw new RuntimeException("织入代码异常");
            }
            targetClass = tClass;
            try {
                targetMethod = tClass.getDeclaredMethod(targetMethodName,classes);
            } catch (NoSuchMethodException e) {
                targetMethod = tClass.getMethod(targetMethodName,classes);
            }
            try {
                originalMethod = tClass.getDeclaredMethod(originalMethodName,classes);
            } catch (NoSuchMethodException e) {
                originalMethod = tClass.getMethod(originalMethodName,classes);
            }
            targetMethod.setAccessible(true);
            originalMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public String[] getArgClassNames() {
        return mArgClassNames;
    }

    public void setArgClassNames(String[] argClassNames) {
        this.mArgClassNames = argClassNames;
    }
}
