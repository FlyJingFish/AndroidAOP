package com.flyjingfish.androidaop

import android.Manifest
import android.os.Bundle
import com.flyjingfish.android_aop_core.annotations.DoubleClick
import com.flyjingfish.android_aop_core.annotations.Permission
import com.flyjingfish.test_lib.BaseActivity

class ThirdActivity : BaseActivity() {
    companion object{
        fun start(activity: MainActivity,listener:OnPhotoSelectListener?){
            start(activity,1,listener)
        }
        @DoubleClick(300)
        fun start(activity: MainActivity,maxSelect :Int,listener:OnPhotoSelectListener?){
            listener?.onBack()
        }
    }
    interface OnPhotoSelectListener {
        fun onBack()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
}