package com.flyjingfish.android_aop_annotation;

import com.flyjingfish.android_aop_annotation.utils.InvokeMethod;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import javax.lang.model.type.WildcardType;

/**
 * 切点相关信息类，<a href = "https://github.com/FlyJingFish/AndroidAOP/wiki/ProceedJoinPoint">wiki 文档使用说明</a>
 */
public final class ProceedReturn {
    /**
     * <a href = "https://github.com/FlyJingFish/AndroidAOP/wiki/ProceedJoinPoint#args">wiki 文档使用说明</a>
     */
    @Nullable
    private final Object[] args;
    @Nullable
    private final Object[] originalArgs;
    @Nullable
    private final Object target;
    @NotNull
    private final Class<?> targetClass;
    private Method targetMethod;
    private InvokeMethod targetInvokeMethod;
    private Method originalMethod;
    private OnInvokeListener onInvokeListener;
    private boolean hasNext;
    private final int argCount;
    private final boolean isSuspend;
    private Object suspendContinuation;

    ProceedReturn(@NotNull Class<?> targetClass, Object[] args, @Nullable Object target, boolean isSuspend) {
        this.targetClass = targetClass;
        Object[] fakeArgs;
        if (isSuspend && args != null){
            fakeArgs = new Object[args.length - 1];
            for (int i = 0; i < args.length - 1; i++) {
                fakeArgs[i] = args[i];
            }
            suspendContinuation = args[args.length - 1];
        }else {
            fakeArgs = args;
        }
        this.args = fakeArgs;

        this.target = target;
        this.isSuspend = isSuspend;
        if (fakeArgs != null) {
            this.originalArgs = fakeArgs.clone();
        } else {
            this.originalArgs = null;
        }
        this.argCount = fakeArgs != null ? fakeArgs.length : 0;
    }

    /**
     * 调用切点方法内代码
     *
     * @return 返回切点方法返回值 <a href = "https://github.com/FlyJingFish/AndroidAOP/wiki/ProceedJoinPoint#proceed">wiki 文档使用说明</a>
     */
    @Nullable
    public Object proceed() {
        return proceed(args);
    }

    /**
     * 调用切点方法内代码
     *
     * @param args 切点方法参数数组
     * @return 返回切点方法返回值 <a href = "https://github.com/FlyJingFish/AndroidAOP/wiki/ProceedJoinPoint#proceed">wiki 文档使用说明</a>
     */
    @Nullable
    public Object proceed(Object... args) {
        if (argCount > 0) {
            if (args == null || args.length != argCount) {
                throw new IllegalArgumentException("proceed 所参数个数不对");
            }
        }

        Object[] realArgs;
        if (isSuspend) {
            realArgs = new Object[argCount + 1];
            if (args != null){
                System.arraycopy(args, 0, realArgs, 0, args.length);
            }
            realArgs[argCount] = suspendContinuation;
        } else {
            realArgs = args;
        }

        if (realArgs != null && this.args != null){
            System.arraycopy(realArgs, 0, this.args, 0, this.args.length);
        }

        try {
            Object returnValue = null;
            if (!hasNext) {
                if (targetInvokeMethod != null) {
                    returnValue = targetInvokeMethod.invoke(target, realArgs);
                } else {
                    returnValue = targetMethod.invoke(target, realArgs);
                }
            } else if (onInvokeListener != null) {
                returnValue = onInvokeListener.onInvoke();
            }
            return returnValue;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e.getTargetException());
        }
    }

    void setTargetMethod(Method targetMethod) {
        this.targetMethod = targetMethod;
    }

    void setTargetMethod(InvokeMethod targetMethod) {
        this.targetInvokeMethod = targetMethod;
    }

    void setOriginalMethod(Method originalMethod) {
        this.originalMethod = originalMethod;
    }

    interface OnInvokeListener {
        Object onInvoke();
    }

    void setOnInvokeListener(OnInvokeListener onInvokeListener) {
        this.onInvokeListener = onInvokeListener;
    }

    void setHasNext(boolean hasNext) {
        this.hasNext = hasNext;
    }

    public Class<?> getReturnType() {
        try {
            if (target != null){
                Type[] types = target.getClass().getGenericInterfaces();
                if (types.length >= 1){
                    Type funtion2Type = types[0];
                    if (funtion2Type instanceof ParameterizedType) {
                        Type[] funtion2ArgumentsTypes = ((ParameterizedType) funtion2Type).getActualTypeArguments();
                        if (funtion2ArgumentsTypes.length>=2){
                            Type continuationType = funtion2ArgumentsTypes[1];
                            if (continuationType instanceof ParameterizedType){
                                Type[] continuationTypeArguments = ((ParameterizedType) continuationType).getActualTypeArguments();
                                for (Type type : continuationTypeArguments) {
                                    String className = type.toString().replaceAll("\\? super ","");
                                    return Conversions.getClass_(className);
                                }
                            }
                        }

                    }
                }
            }
        } catch (Exception e) {

        }

        return targetMethod.getReturnType();
    }

}
