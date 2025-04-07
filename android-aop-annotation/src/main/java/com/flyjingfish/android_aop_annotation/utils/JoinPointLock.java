package com.flyjingfish.android_aop_annotation.utils;

import com.flyjingfish.android_aop_annotation.AndroidAopJoinPoint;

import java.util.concurrent.atomic.AtomicReference;

public class JoinPointLock {
    private final AtomicReference<AndroidAopJoinPoint> joinPoint = new AtomicReference<>();

    public AndroidAopJoinPoint getJoinPoint() {
        return joinPoint.get();
    }

    public void setJoinPoint(AndroidAopJoinPoint joinPoint) {
        this.joinPoint.set(joinPoint);
    }
}
