package com.flyjingfish.androidaop;


import com.flyjingfish.android_aop_core.annotations.Permission;

public class StaticClass {
//    @Permission({android.Manifest.permission.READ_EXTERNAL_STORAGE,android.Manifest.permission.WRITE_EXTERNAL_STORAGE})
    public static void onStaticPermission(MainActivity activity, int maxSelect , ThirdActivity.OnPhotoSelectListener back){
        back.onBack();
    }
}
