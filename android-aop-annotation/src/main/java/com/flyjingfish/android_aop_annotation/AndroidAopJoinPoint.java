package com.flyjingfish.android_aop_annotation;


import com.flyjingfish.android_aop_annotation.base.BasePointCut;
import com.flyjingfish.android_aop_annotation.base.BasePointCutCreator;
import com.flyjingfish.android_aop_annotation.base.BasePointCutSuspend;
import com.flyjingfish.android_aop_annotation.base.MatchClassMethod;
import com.flyjingfish.android_aop_annotation.base.MatchClassMethodCreator;
import com.flyjingfish.android_aop_annotation.base.MatchClassMethodSuspend;
import com.flyjingfish.android_aop_annotation.base.OnBaseSuspendReturnListener;
import com.flyjingfish.android_aop_annotation.ex.AndroidAOPPointCutNotFoundException;
import com.flyjingfish.android_aop_annotation.ex.AndroidAOPSuspendReturnException;
import com.flyjingfish.android_aop_annotation.impl.AopMethodImpl;
import com.flyjingfish.android_aop_annotation.impl.JoinPointBridge;
import com.flyjingfish.android_aop_annotation.impl.ProceedReturnImpl;
import com.flyjingfish.android_aop_annotation.utils.AndroidAOPDebugUtils;
import com.flyjingfish.android_aop_annotation.utils.AndroidAopBeanUtils;
import com.flyjingfish.android_aop_annotation.utils.InvokeMethod;
import com.flyjingfish.android_aop_annotation.utils.InvokeMethods;
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
    private final Object mTarget;
    private final Class<?> targetClass;
    private Class<?>[] mArgClasses;
    private Class<?> mReturnClass;
    private String[] mParamNames;
    private final String targetMethodName;
    private final String originalMethodName;
    private Method targetMethod;
    private Method targetStaticMethod;
    private Method originalMethod;
    private String[] cutMatchClassNames;
    private final String targetClassName;
    private InvokeMethod invokeMethod;
    private Class<?> invokeStaticClass;
    private final boolean lambda;
    private final String classMethodKey;
    private AopMethod aopMethod;
    private List<PointCutAnnotation> pointCutAnnotations;
    private boolean reflectStatic = false;
    private boolean suspendMethod = false;
    private boolean initHasNextAop;


    public AndroidAopJoinPoint(String classMethodKey, Class<?> clazz, String originalMethodName, String targetMethodName, boolean lambda,Object target) {
        this.classMethodKey = classMethodKey;
        this.targetClassName = clazz.getName();
        this.originalMethodName = originalMethodName;
        this.targetMethodName = targetMethodName;
        this.targetClass = clazz;
        this.lambda = lambda;
        this.mTarget = target;
    }

    public Object joinPointReturnExecute(Object[] args,Class returnTypeClassName) throws Throwable {
        Object target = mTarget;
        Object[] returnValue = AndroidAopBeanUtils.INSTANCE.borrowReturnObject();
        ProceedReturnImpl proceedReturn = new ProceedReturnImpl(targetClass, args,target);
        proceedReturn.setReturnType$android_aop_annotation(returnTypeClassName);
        proceedReturn.setOriginalMethod$android_aop_annotation(originalMethod);
        proceedReturn.setTargetMethod$android_aop_annotation(targetMethod);
        proceedReturn.setTargetMethod$android_aop_annotation(invokeMethod);
        proceedReturn.setStaticMethod$android_aop_annotation(targetStaticMethod);
        Object startSuspend = Utils.INSTANCE.getStartSuspendObj(target);

        final List<OnBaseSuspendReturnListener> suspendReturnListeners = AndroidAopBeanUtils.INSTANCE.getSuspendReturnListeners(startSuspend);
        AndroidAopBeanUtils.INSTANCE.removeReturnListener(startSuspend);

        if (suspendReturnListeners != null && !suspendReturnListeners.isEmpty()){
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
        return AndroidAopBeanUtils.INSTANCE.releaseReturnObject(returnValue);
    }
    public Object joinPointExecute(Object[] args,Continuation continuation) throws Throwable {
        Object target = mTarget;
        boolean isSuspend = suspendMethod;
        Object[] returnValue = AndroidAopBeanUtils.INSTANCE.borrowReturnObject();

        ProceedJoinPoint proceedJoinPoint;
        if (isSuspend){
            proceedJoinPoint = JoinPointBridge.getJoinPointSuspend(targetClass, args,target,true,targetMethod,invokeMethod, aopMethod);
        }else {
            proceedJoinPoint = JoinPointBridge.getJoinPoint(targetClass, args,target,false,targetMethod,invokeMethod, aopMethod);
        }
        JoinPointBridge.setStaticMethod(proceedJoinPoint,targetStaticMethod);


        ListIterator<PointCutAnnotation> iterator = pointCutAnnotations.listIterator();
        JoinPointBridge.setHasNext(proceedJoinPoint, initHasNextAop);
        if (!iterator.hasNext()){
            if (AndroidAOPDebugUtils.INSTANCE.isApkDebug()){
                throw new AndroidAOPPointCutNotFoundException("在"+targetClassName + "." + originalMethodName+"上没有找到切面处理类，请 clean 项目并重新编译");
            }else {
                return proceedJoinPoint.proceed();
            }
        }


        if (initHasNextAop) {
            JoinPointBridge.setOnInvokeListener(proceedJoinPoint, () -> {
                if (iterator.hasNext()) {
                    PointCutAnnotation nextCutAnnotation = iterator.next();
                    JoinPointBridge.setHasNext(proceedJoinPoint,iterator.hasNext());
                    Object value = invoke(nextCutAnnotation,isSuspend,proceedJoinPoint,continuation);
                    returnValue[0] = value;
                    return value;
                }else {
                    return returnValue[0];
                }
            });
        }


        PointCutAnnotation cutAnnotation = iterator.next();
        returnValue[0] = invoke(cutAnnotation,isSuspend,proceedJoinPoint,continuation);
        return AndroidAopBeanUtils.INSTANCE.releaseReturnObject(returnValue);
    }

    private static Object invoke(PointCutAnnotation nextCutAnnotation,boolean isSuspend,ProceedJoinPoint proceedJoinPoint,Continuation continuation) throws Throwable{
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
        return value;
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

    public void setInvokeMethod(InvokeMethod invokeMethod,boolean suspendMethod) {
        reflectStatic = invokeMethod instanceof InvokeMethods;
        if (reflectStatic){
            this.invokeStaticClass = invokeMethod.getClass();
        }else {
            this.invokeMethod = invokeMethod;
        }
        this.suspendMethod = suspendMethod;
        getTargetMethod();

        if (aopMethod == null){
            aopMethod = new AopMethodImpl(originalMethod,suspendMethod,mParamNames,mArgClasses,mReturnClass,lambda);
        }
        if (pointCutAnnotations == null){
            Annotation[] annotations = originalMethod.getAnnotations();
            pointCutAnnotations = new ArrayList<>();
            for (Annotation annotation : annotations) {
                String annotationName = annotation.annotationType().getName();
                BasePointCutCreator cutCreator = AndroidAopBeanUtils.INSTANCE.getCutClassCreator(annotationName);
                if (cutCreator != null){
                    BasePointCut<Annotation> basePointCut = (BasePointCut<Annotation>) cutCreator.newInstance();
                    PointCutAnnotation pointCutAnnotation = new PointCutAnnotation(annotation, basePointCut);
                    pointCutAnnotations.add(pointCutAnnotation);
                }
            }

            if (cutMatchClassNames != null) {
                for (String cutMatchClassName : cutMatchClassNames) {
                    MatchClassMethodCreator methodCreator = AndroidAopBeanUtils.INSTANCE.getMatchClassCreator(cutMatchClassName);
                    if (methodCreator != null){
                        MatchClassMethod matchClassMethod = methodCreator.newInstance();
                        PointCutAnnotation pointCutAnnotation = new PointCutAnnotation(matchClassMethod);
                        pointCutAnnotations.add(pointCutAnnotation);
                    }
                }
            }
        }

        initHasNextAop = pointCutAnnotations.size() > 1;
    }

    private void getTargetMethod(){
        if (targetMethod != null && originalMethod != null){
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
            if (reflectStatic && invokeStaticClass != null){
                targetStaticMethod = invokeStaticClass.getDeclaredMethod(classMethodKey, Object.class,Object[].class);
                targetStaticMethod.setAccessible(true);
            }
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
