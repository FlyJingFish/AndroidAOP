package com.flyjingfish.android_aop_annotation;

import androidx.annotation.RequiresApi;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.List;

/**
 * 此类持有执行方法的反射信息，且进行过缓存了，可放心使用
 */
public final class AopMethod {
    private final Method targetMethod;
    private final boolean isSuspend;
    private final Object suspendContinuation;

    private final String[] mParamNames;
    private final Class<?> mReturnType;
    private final Class<?>[] mParamClasses;

    AopMethod(Method targetMethod, boolean isSuspend, Object suspendContinuation, String[] paramNames,Class<?>[] paramClasses,Class<?> returnType) {
        this.targetMethod = targetMethod;
        this.isSuspend = isSuspend;
        this.suspendContinuation = suspendContinuation;
        this.mParamNames = paramNames;
        this.mParamClasses = paramClasses;
        this.mReturnType = returnType;
    }

    public String getName() {
        return targetMethod.getName();
    }

    public String[] getParameterNames() {
        if (isSuspend && mParamNames.length > 0){
            String[] newNames = new String[mParamNames.length - 1];
            System.arraycopy(mParamNames, 0, newNames, 0, newNames.length);
            return newNames;
        }

        return mParamNames;
    }

    /**
     *
     * @return 如果切点函数是 suspend 函数并且返回类型是基本数据类型，会自动转化为包装类型
     */
    public Class<?> getReturnType() {
        if (mReturnType != null){
            return mReturnType;
        }
        return targetMethod.getReturnType();
    }

    public Type getGenericReturnType() {
        if (isSuspend){
            Type[] types = targetMethod.getGenericParameterTypes();
            Type types1 = types[types.length-1];
            if (types1 instanceof ParameterizedType){
                Type[] realTypes = ((ParameterizedType) types1).getActualTypeArguments();
                if (realTypes.length > 0) {
                    Type continuationType = realTypes[0];
                    try {
                        Field field = continuationType.getClass().getDeclaredField("superBound");
                        field.setAccessible(true);
                        Object superBoundObj = field.get(continuationType);
                        Field typesField= superBoundObj.getClass().getDeclaredField("types");
                        typesField.setAccessible(true);
                        List<Type> typesList= (List<Type>) typesField.get(superBoundObj);
                        return typesList.get(0);
                    } catch (Throwable e) {
                    }
                }
            }
        }
        return targetMethod.getGenericReturnType();
    }

    public Class<?> getDeclaringClass() {
        return targetMethod.getDeclaringClass();
    }

    public Class<?>[] getParameterTypes() {
        Class<?>[] cls;
        if (mParamClasses != null){
            cls = mParamClasses;
        }else {
            cls = targetMethod.getParameterTypes();
        }
        if (isSuspend){
            Class<?>[] newCls = new Class[cls.length - 1];
            System.arraycopy(cls, 0, newCls, 0, newCls.length);
            return newCls;
        }

        return cls;
    }

    public Type[] getGenericParameterTypes() {
        Type[] types = targetMethod.getGenericParameterTypes();
        if (isSuspend){
            Type[] newTypes = new Class[types.length - 1];
            System.arraycopy(types, 0, newTypes, 0, newTypes.length);
            return newTypes;
        }
        return types;
    }

    public int getModifiers() {
        return targetMethod.getModifiers();
    }

    public Annotation[] getAnnotations() {
        return targetMethod.getAnnotations();
    }

    public <T extends Annotation> T getAnnotation(Class<T> var1) {
        return targetMethod.getAnnotation(var1);
    }
    @RequiresApi(api = 26)
    public Parameter[] getParameters() {
        Parameter[] parameters = targetMethod.getParameters();
        if (isSuspend && parameters.length > 0){
            Parameter[] newParameters = new Parameter[parameters.length - 1];
            System.arraycopy(parameters, 0, newParameters, 0, newParameters.length);
            return newParameters;
        }
        return parameters;
    }
    public Annotation[][] getParameterAnnotations() {
        Annotation[][] parameterAnnotations = targetMethod.getParameterAnnotations();
        if (isSuspend && parameterAnnotations.length > 0){
            Annotation[][] newParameterAnnotations = new Annotation[parameterAnnotations.length - 1][];
            System.arraycopy(parameterAnnotations, 0, newParameterAnnotations, 0, newParameterAnnotations.length);
            return newParameterAnnotations;
        }
        return parameterAnnotations;
    }
}
