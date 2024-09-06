package com.flyjingfish.test_lib

import android.app.Application
import android.content.Context
import android.view.Gravity
import android.widget.Toast

object ToastUtils {
    private var mToast: Toast? = null
    lateinit var app:Application

    fun makeText(text: CharSequence) {
        makeText(app, text, Toast.LENGTH_SHORT)?.show()
    }

    fun makeText(context: Context, text: CharSequence) {
        makeText(context.applicationContext, text, Toast.LENGTH_SHORT)?.show()
    }

    private fun makeText(context: Context, text: CharSequence, duration: Int): Toast? {
        mToast?.cancel()
        try {
            mToast = Toast.makeText(context, text, duration)
            mToast?.setGravity(Gravity.BOTTOM,0,0)
        } catch (e: NullPointerException) {
            e.printStackTrace()
            mToast = Toast.makeText(context, text, duration)
        }
        return mToast
    }
}