package com.flyjingfish.android_aop_core

import android.app.Application
import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri

class AndroidAopContentProvider : ContentProvider() {
    companion object{
        private var appContext: Context? = null
        fun getAppContext(): Context {
            return appContext ?: throw IllegalStateException("AndroidAopContentProvider not init")
        }

        /**
         * 如果不想启动 AndroidAopContentProvider，需要手动设置此项
         */
        fun setAppContext(application: Application){
            appContext = application
        }
    }
    override fun onCreate(): Boolean {
        appContext = context!!
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor? {
        return null
    }

    override fun getType(uri: Uri): String? {
        return null
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        return null
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        return 0
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        return 0
    }
}