package com.flyjingfish.test_lib

import android.content.Context
import android.widget.Toast

object ToastUtils {
    private var mToast: Toast? = null

    fun makeText(context: Context, text: CharSequence) {
        makeText(context, text, Toast.LENGTH_SHORT)?.show()
    }

    private fun makeText(context: Context, text: CharSequence, duration: Int): Toast? {
        mToast?.cancel()
        try {
            mToast = Toast.makeText(context, null, duration)
            mToast?.setText(text)
        } catch (e: NullPointerException) {
            e.printStackTrace()
            mToast = Toast.makeText(context, text, duration)
        }
        return mToast
    }
}