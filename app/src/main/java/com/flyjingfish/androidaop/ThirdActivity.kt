package com.flyjingfish.androidaop

import android.Manifest
import android.os.Bundle
import com.flyjingfish.android_aop_core.annotations.Permission
import com.flyjingfish.test_lib.BaseActivity

class ThirdActivity : BaseActivity() {
    companion object{
        fun start(activity: BaseActivity,listener:OnPhotoSelectListener){
            start(activity,1,listener)
        }
        @Permission(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        fun start(activity: BaseActivity,maxSelect :Int,listener:OnPhotoSelectListener?){
//            activity.setLogcat("测试静态方法，activity$activity,maxSelect=$maxSelect")
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