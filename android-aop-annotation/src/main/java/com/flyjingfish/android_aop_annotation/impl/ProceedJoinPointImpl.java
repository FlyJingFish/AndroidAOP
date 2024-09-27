package com.flyjingfish.android_aop_annotation.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.flyjingfish.android_aop_annotation.AopMethod;
import com.flyjingfish.android_aop_annotation.ProceedJoinPoint;
import com.flyjingfish.android_aop_annotation.base.OnBaseSuspendReturnListener;
import com.flyjingfish.android_aop_annotation.ex.AndroidAOPIllegalArgumentException;
import com.flyjingfish.android_aop_annotation.utils.AndroidAopBeanUtils;
import com.flyjingfish.android_aop_annotation.utils.InvokeMethod;
import com.flyjingfish.android_aop_annotation.utils.Utils;

import java.lang.reflect.Method;

/**
 * 切点相关信息类，<a href = "https://github.com/FlyJingFish/AndroidAOP/wiki/ProceedJoinPoint">wiki 文档使用说明</a>
 */
class ProceedJoinPointImpl implements ProceedJoinPoint {
    /**
     * <a href = "https://github.com/FlyJingFish/AndroidAOP/wiki/ProceedJoinPoint#args">wiki 文档使用说明</a>
     */
    @Nullable
    private final Object[] args;
    @Nullable
    private final Object[] originalArgs;
    @Nullable
    private final Object target;
    @NonNull
    private final Class<?> targetClass;
    private final Method targetMethod;
    @Nullable
    private final InvokeMethod targetInvokeMethod;
    private final AopMethod targetAopMethod;
    private final int argCount;
    private final boolean isSuspend;
    private final Object suspendContinuation;
    private OnInvokeListener onInvokeListener;
    private boolean hasNext;

    ProceedJoinPointImpl(@NonNull Class<?> targetClass, Object[] args, @Nullable Object target, boolean isSuspend,
                         Method targetMethod, @Nullable InvokeMethod invokeMethod, AopMethod aopMethod) {
        this.targetClass = targetClass;
        this.target = target;
        this.isSuspend = isSuspend;
        this.targetMethod = targetMethod;
        this.targetInvokeMethod = invokeMethod;
        this.targetAopMethod = aopMethod;

        Object[] fakeArgs;
        if (isSuspend && args != null) {
            fakeArgs = new Object[args.length - 1];
            System.arraycopy(args, 0, fakeArgs, 0, args.length - 1);
            suspendContinuation = args[args.length - 1];
        } else {
            fakeArgs = args;
            suspendContinuation = null;
        }
        this.args = fakeArgs;

        if (fakeArgs != null) {
            this.originalArgs = fakeArgs.clone();
        } else {
            this.originalArgs = null;
        }
        this.argCount = fakeArgs != null ? fakeArgs.length : 0;
    }

    @Nullable
    @Override
    public Object[] getArgs() {
        return args;
    }

    /**
     * 调用切点方法内代码
     *
     * @return 返回切点方法返回值 <a href = "https://github.com/FlyJingFish/AndroidAOP/wiki/ProceedJoinPoint#proceed">wiki 文档使用说明</a>
     */
    @Nullable
    @Override
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
    @Override
    public Object proceed(Object... args) {
        return realProceed(null, args);
    }

    /**
     * @return 切点方法相关信息
     */
    @NonNull
    @Override
    public AopMethod getTargetMethod() {
        return targetAopMethod;
    }

    /**
     * @return 切点方法所在对象，如果方法为静态的，此值为null
     */
    @Nullable
    @Override
    public Object getTarget() {
        return target;
    }

    /**
     * @return 切点方法所在类 Class
     */
    @NonNull
    @Override
    public Class<?> getTargetClass() {
        return targetClass;
    }

    /**
     * 和 {@link ProceedJoinPointImpl#args} 相比，返回的引用地址不同，但数组里边的对象一致
     *
     * @return 最开始进入方法时的参数  <a href = "https://github.com/FlyJingFish/AndroidAOP/wiki/ProceedJoinPoint#args">wiki 文档使用说明</a>
     */
    @Nullable
    @Override
    public Object[] getOriginalArgs() {
        return originalArgs;
    }


    @Nullable
    Object realProceed(OnBaseSuspendReturnListener onSuspendReturnListener, Object... args) {
        if (argCount > 0) {
            if (args == null || args.length != argCount) {
                throw new AndroidAOPIllegalArgumentException("proceed 所参数个数不对");
            }
        }

        Object[] realArgs;
        if (isSuspend) {
            realArgs = new Object[argCount + 1];
            if (args != null) {
                System.arraycopy(args, 0, realArgs, 0, args.length);
            }
            realArgs[argCount] = suspendContinuation;
        } else {
            realArgs = args;
        }

        if (realArgs != null && this.args != null) {
            System.arraycopy(realArgs, 0, this.args, 0, this.args.length);
        }

        try {
            Object returnValue = null;
            setReturnListener(onSuspendReturnListener);
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
        } catch (Throwable e) {
            throw Utils.INSTANCE.getRealRuntimeException(e);
        }
    }

    private void setReturnListener(OnBaseSuspendReturnListener onSuspendReturnListener) {
        if (isSuspend && onSuspendReturnListener != null && suspendContinuation != null) {
            Object key1 = suspendContinuation;
            AndroidAopBeanUtils.INSTANCE.addSuspendReturnListener(key1, onSuspendReturnListener);
            try {
                Method method = suspendContinuation.getClass().getMethod("getCompletion");
                method.setAccessible(true);
                Object key2 = method.invoke(suspendContinuation);
                if (key2 != null) {
                    AndroidAopBeanUtils.INSTANCE.addSuspendReturnListener(key2, onSuspendReturnListener);
                    AndroidAopBeanUtils.INSTANCE.saveReturnKey(key1, key2);
                }
            } catch (Throwable ignored) {
            }

        }
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

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("JoinPoint[").append(targetClass.getName())
                .append(".")
                .append(targetAopMethod.getName())
                .append("(");
        Class<?>[] paramsClasses = targetAopMethod.getParameterTypes();
        for (Class<?> paramsClass : paramsClasses) {
            buf.append(paramsClass.getName()).append(",");
        }
        if (paramsClasses.length > 0){
            buf.setLength(buf.length() - 1);
        }
        buf.append(")]");
        return buf.toString();
    }
}
