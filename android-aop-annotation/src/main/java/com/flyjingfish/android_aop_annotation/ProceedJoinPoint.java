package com.flyjingfish.android_aop_annotation;

import com.flyjingfish.android_aop_annotation.base.OnBaseSuspendReturnListener;
import com.flyjingfish.android_aop_annotation.utils.AndroidAopBeanUtils;
import com.flyjingfish.android_aop_annotation.utils.InvokeMethod;
import com.flyjingfish.android_aop_annotation.utils.Utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

/**
 * 切点相关信息类，<a href = "https://github.com/FlyJingFish/AndroidAOP/wiki/ProceedJoinPoint">wiki 文档使用说明</a>
 */
public class ProceedJoinPoint {
    /**
     * <a href = "https://github.com/FlyJingFish/AndroidAOP/wiki/ProceedJoinPoint#args">wiki 文档使用说明</a>
     */
    @Nullable
    public final Object[] args;
    @Nullable
    private final Object[] originalArgs;
    @Nullable
    public final Object target;
    @NotNull
    public final Class<?> targetClass;
    private Method targetMethod;
    private InvokeMethod targetInvokeMethod;
    private Method originalMethod;
    private AopMethod targetAopMethod;
    private OnInvokeListener onInvokeListener;
    private boolean hasNext;
    private final int argCount;
    private final boolean isSuspend;
    private Object suspendContinuation;
    private Object methodReturnValue;

    ProceedJoinPoint(@NotNull Class<?> targetClass, Object[] args, @Nullable Object target, boolean isSuspend) {
        this.targetClass = targetClass;
        Object[] fakeArgs;
        if (isSuspend && args != null){
            fakeArgs = new Object[args.length - 1];
            System.arraycopy(args, 0, fakeArgs, 0, args.length - 1);
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
        return realProceed(null,args);
    }

    @Nullable
    Object realProceed(OnBaseSuspendReturnListener onSuspendReturnListener, Object... args) {
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
            setReturnListener(onSuspendReturnListener);
            if (!hasNext) {
                if (targetInvokeMethod != null) {
                    returnValue = targetInvokeMethod.invoke(target, realArgs);
                } else {
                    returnValue = targetMethod.invoke(target, realArgs);
                }
                methodReturnValue = returnValue;
            } else if (onInvokeListener != null) {
                returnValue = onInvokeListener.onInvoke();
            }

            return returnValue;
        } catch (Throwable e) {
            throw Utils.INSTANCE.getRealRuntimeException(e);
        }
    }

    private void setReturnListener(OnBaseSuspendReturnListener onSuspendReturnListener){
        if (isSuspend && onSuspendReturnListener != null && suspendContinuation != null){
            Object key1 = suspendContinuation;
            AndroidAopBeanUtils.INSTANCE.addSuspendReturnListener(key1,onSuspendReturnListener);
            try {
                Method method = suspendContinuation.getClass().getMethod("getCompletion");
                method.setAccessible(true);
                Object key2 = method.invoke(suspendContinuation);
                if (key2 != null){
                    AndroidAopBeanUtils.INSTANCE.addSuspendReturnListener(key2,onSuspendReturnListener);
                    AndroidAopBeanUtils.INSTANCE.saveReturnKey(key1,key2);
                }
            } catch (Throwable e) {
            }

        }
    }

    /**
     * @return 切点方法相关信息
     */
    @NotNull
    public AopMethod getTargetMethod() {
        return targetAopMethod;
    }

    void setTargetMethod(Method targetMethod) {
        this.targetMethod = targetMethod;
    }

    void setTargetMethod(InvokeMethod targetMethod) {
        this.targetInvokeMethod = targetMethod;
    }

    void setOriginalMethod(Method originalMethod) {
        this.originalMethod = originalMethod;
        targetAopMethod = new AopMethod(originalMethod,isSuspend,suspendContinuation);
    }

    /**
     * @return 切点方法所在对象，如果方法为静态的，此值为null
     */
    @Nullable
    public Object getTarget() {
        return target;
    }

    /**
     * @return 切点方法所在类 Class
     */
    @NotNull
    public Class<?> getTargetClass() {
        return targetClass;
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

    /**
     * 和 {@link ProceedJoinPoint#args} 相比，返回的引用地址不同，但数组里边的对象一致
     *
     * @return 最开始进入方法时的参数  <a href = "https://github.com/FlyJingFish/AndroidAOP/wiki/ProceedJoinPoint#args">wiki 文档使用说明</a>
     */
    @Nullable
    public Object[] getOriginalArgs() {
        return originalArgs;
    }

    Object getMethodReturnValue() {
        return methodReturnValue;
    }
}
