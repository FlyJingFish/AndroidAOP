package com.flyjingfish.androidaop;


import com.flyjingfish.android_aop_core.annotations.MainThread;

public abstract class TestInterface {
    @MainThread
    public abstract void onTest();
}
