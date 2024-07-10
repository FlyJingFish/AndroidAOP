package com.flyjingfish.androidaop;

import android.os.Bundle;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;

import com.flyjingfish.android_aop_core.annotations.Delay;
import com.flyjingfish.android_aop_core.annotations.MainThread;
import com.flyjingfish.android_aop_core.annotations.Scheduled;
import com.flyjingfish.android_aop_core.utils.AndroidAop;
import com.flyjingfish.androidaop.databinding.ActivitySecondBinding;
import com.flyjingfish.androidaop.test.MyOnClickListener2;
import com.flyjingfish.test_lib.annotation.MyAnno;

import java.lang.reflect.Method;

import kotlin.coroutines.Continuation;
import kotlinx.coroutines.BuildersKt;

public class SecondActivity extends BaseActivity2 {

    private com.flyjingfish.androidaop.databinding.ActivitySecondBinding binding;
    static final String TAG = "SecondActivity";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySecondBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.btnInner.setOnClickListener((MyOnClickListener2) v -> onSingleClick());
        binding.btnOnScheduled1.setOnClickListener(v -> {
            setLogcat("延迟2秒开始onScheduled1,time= "+UtilsKt.getCurrentTime());
            onScheduled1();
        });
        binding.btnOnScheduled2.setOnClickListener(v -> {
            setLogcat("开始onScheduled2,time= "+UtilsKt.getCurrentTime());
            onScheduled2();
        });
        binding.btnOnScheduled1Stop.setOnClickListener(v -> AndroidAop.INSTANCE.shutdownNow("onScheduled1"));
        binding.btnOnScheduled2Stop.setOnClickListener(v -> AndroidAop.INSTANCE.shutdownNow("onScheduled2"));

        binding.btnOnDelay1.setOnClickListener(v -> {
            setLogcat("延迟5秒开始onDelay1,time= "+UtilsKt.getCurrentTime());
            onDelay1();
        });
        binding.btnOnDelay2.setOnClickListener(v -> {
            setLogcat("延迟5秒开始onDelay2,time= "+UtilsKt.getCurrentTime());
            onDelay2();
        });
        binding.btnOnDelay1Stop.setOnClickListener(v -> AndroidAop.INSTANCE.shutdownNow("onDelay1"));
        binding.btnOnDelay2Stop.setOnClickListener(v -> AndroidAop.INSTANCE.shutdownNow("onDelay2"));
        binding.tvLogcat.setOnClickListener(v -> binding.tvLogcat.setText("日志:（点此清除）\n") );

//        Method[] methods = SecondActivity.class.getDeclaredMethods();
//        for (Method method : methods) {
//            Class<?>[] classes = method.getParameterTypes();
//            for (Class<?> aClass : classes) {
//                Log.e(TAG,method.getName()+",aClass="+aClass.getName());
//            }
//        }
    }

    @MyAnno
    public void testIntArray(int[] intArray,int[][] intArray1,int[][][] intArray2,int i,Method[] methods,
                             boolean[] b1,
                             char[] b2,
                             byte[] b3,
                             short[] b4,
                             float[] b5,
                             long[] b6,
                             double[] b7){

    }

    @MyAnno
    private void onSingleClick() {
        testIntArray(new int[]{0,1},null,null,0,null,null,null,null,null,null,null,null);
        Log.e("Test_click", "onSingleClick");
        binding.ivImage.setImageResource(R.mipmap.ic_launcher);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @MainThread
    void setLogcat(String text) {
        binding.tvLogcat.append(text + "\n");
    }

    @Scheduled(id = "onScheduled1", initialDelay = 2000, interval = 1000, repeatCount = 5,isOnMainThread = true)
    void onScheduled1() {
        String test = "onScheduled1,time= " + UtilsKt.getCurrentTime() + ",是否主线程=" + (Looper.getMainLooper() == Looper.myLooper());
        setLogcat(test);
        Log.e(TAG,test);
    }

    @Scheduled(id = "onScheduled2", initialDelay = 0, interval = 1000, repeatCount = 5,isOnMainThread = false)
    void onScheduled2() {
        String test = "onScheduled2,time= " + UtilsKt.getCurrentTime() + ",是否主线程=" + (Looper.getMainLooper() == Looper.myLooper());
        setLogcat(test);
        Log.e(TAG,test);
    }

    @Delay(id = "onDelay1", delay = 5000,isOnMainThread = true)
    void onDelay1() {
        String test = "onDelay1 = " + UtilsKt.getCurrentTime() + ",是否主线程=" + (Looper.getMainLooper() == Looper.myLooper());
        setLogcat(test);
        Log.e(TAG,test);
    }

    @Delay(id = "onDelay2", delay = 5000,isOnMainThread = false)
    void onDelay2() {
        String test = "onDelay2 = " + UtilsKt.getCurrentTime() + ",是否主线程=" + (Looper.getMainLooper() == Looper.myLooper());
        setLogcat(test);
        Log.e(TAG,test);
    }


}
