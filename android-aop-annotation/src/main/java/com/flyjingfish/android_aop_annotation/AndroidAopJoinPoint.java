package com.flyjingfish.android_aop_annotation;


import com.flyjingfish.android_aop_annotation.base.BasePointCut;
import com.flyjingfish.android_aop_annotation.base.BasePointCutSuspend;
import com.flyjingfish.android_aop_annotation.base.MatchClassMethod;
import com.flyjingfish.android_aop_annotation.base.MatchClassMethodSuspend;
import com.flyjingfish.android_aop_annotation.base.OnBaseSuspendReturnListener;
import com.flyjingfish.android_aop_annotation.ex.AndroidAOPPointCutNotFoundException;
import com.flyjingfish.android_aop_annotation.ex.AndroidAOPSuspendReturnException;
import com.flyjingfish.android_aop_annotation.impl.AopMethodImpl;
import com.flyjingfish.android_aop_annotation.impl.JoinPoint;
import com.flyjingfish.android_aop_annotation.impl.ProceedReturnImpl;
import com.flyjingfish.android_aop_annotation.utils.AndroidAOPDebugUtils;
import com.flyjingfish.android_aop_annotation.utils.AndroidAopBeanUtils;
import com.flyjingfish.android_aop_annotation.utils.InvokeMethod;
import com.flyjingfish.android_aop_annotation.utils.MethodMap;
import com.flyjingfish.android_aop_annotation.utils.Utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import kotlin.coroutines.Continuation;

public final class AndroidAopJoinPoint {
    static {
        AndroidAOPDebugUtils.INSTANCE.init$android_aop_annotation();
    }
    private Object mTarget;
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
    private final String targetClassName;
    private InvokeMethod invokeMethod;
    private final boolean lambda;
    private boolean init = false;
    private final String classMethodKey;
    private AopMethod aopMethod;
    private List<PointCutAnnotation> pointCutAnnotations;


    public AndroidAopJoinPoint(String classMethodKey, Class<?> clazz, String originalMethodName, String targetMethodName, boolean lambda) {
        this.classMethodKey = classMethodKey;
        this.targetClassName = clazz.getName();
        this.originalMethodName = originalMethodName;
        this.targetMethodName = targetMethodName;
        this.targetClass = clazz;
        this.lambda = lambda;

    }

    public void setTarget(Object target) {
        this.mTarget = target;
    }


    public boolean isInit() {
        return init;
    }

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
        Object target = mTarget;
        Object[] args = mArgs;
        mTarget = null;
        mArgs = null;
        init = true;
        Object[] returnValue = new Object[1];
        ProceedReturnImpl proceedReturn = new ProceedReturnImpl(targetClass, args,target);
        proceedReturn.setReturnType$android_aop_annotation(returnTypeClassName);
        proceedReturn.setOriginalMethod$android_aop_annotation(originalMethod);
        proceedReturn.setTargetMethod$android_aop_annotation(targetMethod);
        proceedReturn.setTargetMethod$android_aop_annotation(invokeMethod);
        Object startSuspend = Utils.INSTANCE.getStartSuspendObj(target);

        final List<OnBaseSuspendReturnListener> suspendReturnListeners = AndroidAopBeanUtils.INSTANCE.getSuspendReturnListeners(startSuspend);
        AndroidAopBeanUtils.INSTANCE.removeReturnListener(startSuspend);

        if (suspendReturnListeners != null && suspendReturnListeners.size() > 0){
            Iterator<OnBaseSuspendReturnListener> iterator = suspendReturnListeners.iterator();
            if (suspendReturnListeners.size() > 1) {
                proceedReturn.setOnInvokeListener$android_aop_annotation(() -> {
                    if (iterator.hasNext()) {
                        OnBaseSuspendReturnListener listener = iterator.next();
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

            proceedReturn.setHasNext$android_aop_annotation(suspendReturnListeners.size() > 1);
            OnBaseSuspendReturnListener listener = iterator.next();

            returnValue[0] = Utils.INSTANCE.invokeReturn(proceedReturn,listener);
            for (OnBaseSuspendReturnListener onSuspendReturnListener : suspendReturnListeners) {
                AndroidAopBeanUtils.INSTANCE.removeIgnoreOther(onSuspendReturnListener);
            }
            returnValue[0] = Conversions.return2Type(proceedReturn.getReturnType(), returnValue[0]);
        }else {
            returnValue[0] = proceedReturn.proceed();
        }
        return returnValue[0];
    }
    public Object joinPointExecute(Continuation continuation) {
        init = true;
        Object target = mTarget;
        Object[] args = mArgs;
        mTarget = null;
        mArgs = null;
        boolean isSuspend = continuation != null;
        Object[] returnValue = new Object[1];

        ProceedJoinPoint proceedJoinPoint;
        if (aopMethod == null){
            aopMethod = new AopMethodImpl(originalMethod,isSuspend,mParamNames,mArgClasses,mReturnClass,lambda);
        }
        if (isSuspend){
            proceedJoinPoint = JoinPoint.INSTANCE.getJoinPointSuspend(targetClass, args,target,true,targetMethod,invokeMethod, aopMethod);
        }else {
            proceedJoinPoint = JoinPoint.INSTANCE.getJoinPoint(targetClass, args,target,false,targetMethod,invokeMethod, aopMethod);
        }


        if (pointCutAnnotations == null){
            Annotation[] annotations = originalMethod.getAnnotations();
            pointCutAnnotations = new ArrayList<>();
            for (Annotation annotation : annotations) {
                String annotationName = annotation.annotationType().getName();
                if (AndroidAopBeanUtils.INSTANCE.getCutClassCreator(annotationName) != null) {
                    BasePointCut<Annotation> basePointCut = AndroidAopBeanUtils.INSTANCE.getBasePointCut(proceedJoinPoint, annotationName, classMethodKey);
                    PointCutAnnotation pointCutAnnotation = new PointCutAnnotation(annotation, basePointCut);
                    pointCutAnnotations.add(pointCutAnnotation);
                }
            }

            if (cutMatchClassNames != null) {
                for (String cutMatchClassName : cutMatchClassNames) {
                    if (AndroidAopBeanUtils.INSTANCE.getMatchClassCreator(cutMatchClassName) != null){
                        MatchClassMethod matchClassMethod = AndroidAopBeanUtils.INSTANCE.getMatchClassMethod(proceedJoinPoint, cutMatchClassName, classMethodKey);
                        PointCutAnnotation pointCutAnnotation = new PointCutAnnotation(matchClassMethod);
                        pointCutAnnotations.add(pointCutAnnotation);
                    }
                }
            }
        }


        ListIterator<PointCutAnnotation> iterator = pointCutAnnotations.listIterator();
        JoinPoint.INSTANCE.setHasNext(proceedJoinPoint, pointCutAnnotations.size() > 1);
        if (!iterator.hasNext()){
            if (AndroidAOPDebugUtils.INSTANCE.isApkDebug()){
                throw new AndroidAOPPointCutNotFoundException("在"+targetClassName + "." + originalMethodName+"上没有找到切面处理类，请 clean 项目并重新编译");
            }else {
                return proceedJoinPoint.proceed();
            }
        }


        if (pointCutAnnotations.size() > 1) {
            JoinPoint.INSTANCE.setOnInvokeListener(proceedJoinPoint, () -> {
                if (iterator.hasNext()) {
                    PointCutAnnotation nextCutAnnotation = iterator.next();
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
                                        throw e;
                                    }else {
                                        throw new AndroidAOPSuspendReturnException("协程函数的切面不可修改返回值，请使用 BasePointCutSuspend");
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
                                        throw e;
                                    }else {
                                        throw new AndroidAOPSuspendReturnException("协程函数的切面不可修改返回值，请使用 MatchClassMethodSuspend");
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


        PointCutAnnotation cutAnnotation = iterator.next();
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
                            throw e;
                        }else {
                            throw new AndroidAOPSuspendReturnException("协程函数的切面不可修改返回值，请使用 BasePointCutSuspend");
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
                            throw e;
                        }else {
                            throw new AndroidAOPSuspendReturnException("协程函数的切面不可修改返回值，请使用 MatchClassMethodSuspend");
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
        this.mArgs = args;
        getTargetMethod();
    }

    public void setInvokeMethod(InvokeMethod invokeMethod) {
        this.invokeMethod = invokeMethod;
    }

    private void getTargetMethod(){
        if (targetMethod != null && originalMethod != null){
            return;
        }

        String key = classMethodKey +"-" + System.identityHashCode(mTarget);
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
            AndroidAopBeanUtils.INSTANCE.putMethodMapCache(key,methodMap, mTarget);
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
