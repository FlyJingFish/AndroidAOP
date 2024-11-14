package com.flyjingfish.androidaop.test;

import android.util.Log;

import com.flyjingfish.test_lib.annotation.MyAnno;

import org.bouncycastle.util.test.Test;


public class Round extends Base2{
    public Runnable runnable;


   public Round() {
    }

    @MyAnno
    public Runnable getRunnable() {
        Log.e("Round","setRunnable");
        return runnable;
    }

    public void setRunnable(Runnable runnable) {
        this.runnable = runnable;
        Log.e("Round","setRunnable");
        Log.e("Round","setRunnable");
        Log.e("Round","setRunnable");
    }

    public int getNumber() {
        return 1;
    }

    public Test getTest(Test test) {
        return test;
    }
}
