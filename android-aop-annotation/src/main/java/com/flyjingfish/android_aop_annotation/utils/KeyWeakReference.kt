package com.flyjingfish.android_aop_annotation.utils

import java.lang.ref.ReferenceQueue
import java.lang.ref.WeakReference

class KeyWeakReference<T>(referent: T, q: ReferenceQueue<in T>,val key:String) : WeakReference<T>(referent, q)