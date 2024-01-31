package com.flyjingfish.android_aop_plugin.beans

data class ReplaceMethodInfo(
    var oldOwner: String,
    var oldMethodName: String,
    var oldMethodDesc: String,
    var newOwner: String,
    var newMethodName: String,
    var newMethodDesc: String
)