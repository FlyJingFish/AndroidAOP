package com.flyjingfish.android_aop_annotation.utils;

import com.flyjingfish.android_aop_annotation.AndroidAopJoinPoint;

import java.util.concurrent.atomic.AtomicReference;

public class JoinPointLock {
    private volatile AndroidAopJoinPoint joinPoint;

    public AndroidAopJoinPoint getJoinPoint() {
        return joinPoint;
    }

    public void setJoinPoint(AndroidAopJoinPoint joinPoint) {
        this.joinPoint = joinPoint;
    }
}
