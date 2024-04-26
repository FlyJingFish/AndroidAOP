package com.flyjingfish.android_aop_annotation;

import com.flyjingfish.android_aop_annotation.utils.InvokeMethod;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * 切点相关信息类，<a href = "https://github.com/FlyJingFish/AndroidAOP/wiki/ProceedJoinPoint">wiki 文档使用说明</a>
 */
public final class ProceedJoinPoint {
    /**
     *  <a href = "https://github.com/FlyJingFish/AndroidAOP/wiki/ProceedJoinPoint#args">wiki 文档使用说明</a>
     */
    @Nullable
    public Object[] args;
    @Nullable
    private Object[] originalArgs;
    @Nullable
    public Object target;
    @NotNull
    public Class<?> targetClass;
    private Method targetMethod;
    private InvokeMethod targetInvokeMethod;
    private Method originalMethod;
    private AopMethod targetAopMethod;
    private OnInvokeListener onInvokeListener;
    private boolean hasNext;
    private final int argCount;

    ProceedJoinPoint(@NotNull Class<?> targetClass,Object[] args) {
        this.targetClass = targetClass;
        this.args = args;
        if (args != null){
            this.originalArgs = args.clone();
        }
        this.argCount = args != null ? args.length : 0;
    }

    /**
     * 调用切点方法内代码
     * @return 返回切点方法返回值 <a href = "https://github.com/FlyJingFish/AndroidAOP/wiki/ProceedJoinPoint#proceed">wiki 文档使用说明</a>
     */
    @Nullable
    public Object proceed(){
        return proceed(args);
    }

    /**
     * 调用切点方法内代码
     * @param args 切点方法参数数组
     * @return 返回切点方法返回值 <a href = "https://github.com/FlyJingFish/AndroidAOP/wiki/ProceedJoinPoint#proceed">wiki 文档使用说明</a>
     */
    @Nullable
    public Object proceed(Object... args){
        this.args = args;
        if (argCount > 0){
            if (args == null || args.length != argCount){
                throw new IllegalArgumentException("proceed 所参数个数不对");
            }
        }
        Object returnValue = null;
        if (!hasNext){
            returnValue = targetInvokeMethod.invoke(target,args);
        }else if (onInvokeListener != null){
            returnValue = onInvokeListener.onInvoke();
        }
        return returnValue;
//        try {
//            Object returnValue = null;
//            if (!hasNext){
//                if (targetInvokeMethod != null){
//                    returnValue = targetInvokeMethod.invoke(target,args);
//                }else {
//                    returnValue = targetMethod.invoke(target,args);
//                }
//            }else if (onInvokeListener != null){
//                returnValue = onInvokeListener.onInvoke();
//            }
//            return returnValue;
//        } catch (IllegalAccessException e) {
//            throw new RuntimeException(e);
//        } catch (InvocationTargetException e) {
//            throw new RuntimeException(e.getTargetException());
//        }
    }

    /**
     *
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
        targetAopMethod = new AopMethod(originalMethod);
    }

    /**
     *
     * @return 切点方法所在对象，如果方法为静态的，此值为null
     */
    @Nullable
    public Object getTarget() {
        return target;
    }

    /**
     *
     * @return 切点方法所在类 Class
     */
    @NotNull
    public Class<?> getTargetClass() {
        return targetClass;
    }

    void setTargetClass(@NotNull Class<?> targetClass) {
        this.targetClass = targetClass;
    }

    interface OnInvokeListener{
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
     * @return 最开始进入方法时的参数  <a href = "https://github.com/FlyJingFish/AndroidAOP/wiki/ProceedJoinPoint#args">wiki 文档使用说明</a>
     */
    @Nullable
    public Object[] getOriginalArgs() {
        return originalArgs;
    }
}
