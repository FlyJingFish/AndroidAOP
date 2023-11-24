package com.flyjingfish.androidaop;

import android.util.Log;

import com.flyjingfish.test_lib.annotation.MyAnno;


public class Round {
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
}
