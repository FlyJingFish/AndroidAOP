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
            Class[] classes = null;
            if (args != null && args.length > 0){
                classes = new Class[args.length];
                int index = 0;
                for (String className : mArgClassNames) {
                    try {
                        Class c = Class.forName(className);
                        classes[index] = c;
                    } catch (ClassNotFoundException ignored) {
                    }

                    index++;
                }
            }
            if (classes != null){
                if (cutMatchClassName != null){
                    targetMethod = target.getClass().getMethod(targetMethodName,classes);
                }else {
                    targetMethod = target.getClass().getDeclaredMethod(targetMethodName,classes);
                }
                originalMethod = target.getClass().getDeclaredMethod(originalMethodName,classes);
            }else {
                if (cutMatchClassName != null){
                    targetMethod = target.getClass().getMethod(targetMethodName);
                }else {
                    targetMethod = target.getClass().getDeclaredMethod(targetMethodName);
                }
                originalMethod = target.getClass().getDeclaredMethod(originalMethodName);
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
