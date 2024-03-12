package com.flyjingfish.androidaop

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.flyjingfish.android_aop_annotation.anno.AndroidAopModifyExtendsClass

@AndroidAopModifyExtendsClass("com.flyjingfish.test_lib.BaseActivity")
abstract class ReplaceBaseActivity :AppCompatActivity() {
    override fun onResume() {
        super.onResume()
        Log.e("ReplaceBaseActivity","ReplaceBaseActivity-onResume")
    }
}