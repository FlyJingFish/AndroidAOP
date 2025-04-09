package com.flyjingfish.android_aop_plugin.beans

import org.objectweb.asm.Type

data class ReplaceMethodInfo(
    var oldOwner: String,
    var oldMethodName: String,
    var oldMethodDesc: String,
    var newOwner: String,
    var newMethodName: String,
    var newMethodDesc: String,
    var replaceType:ReplaceType = ReplaceType.METHOD,
    var newClassName :String = ""
){
    private var isCallNew:Boolean?=null
    private var isDeleteNew:Boolean?=null
    enum class ReplaceType{
        METHOD,INIT,NEW
    }
    fun getReplaceKey():String{
        return oldOwner + oldMethodName + oldMethodDesc
    }
    fun getReplaceJsonKey():String{
        return oldOwner + newOwner
    }
    fun checkAvailable():Boolean{
        return oldOwner.isNotEmpty() && oldMethodName.isNotEmpty() && oldMethodDesc.isNotEmpty() 
                && newOwner.isNotEmpty() && newMethodName.isNotEmpty() && newMethodDesc.isNotEmpty()
    }

    fun isCallNew():Boolean{
        val oldCallNew = isCallNew
        if (oldCallNew != null){
            return oldCallNew
        }
        val callNew = if (replaceType == ReplaceType.NEW){
            val type = Type.getReturnType(newMethodDesc)
            type.descriptor != "V"
        }else{
            false
        }
        isCallNew = callNew
        return callNew
    }

    fun isDeleteNew():Boolean{
        val oldDeleteNew = isDeleteNew
        if (oldDeleteNew != null){
            return oldDeleteNew
        }
        val deleteNew = if (replaceType == ReplaceType.INIT){
            val newReplace = Type.getArgumentTypes(newMethodDesc).joinToString("")
            val oldReplace = Type.getArgumentTypes(oldMethodDesc).joinToString("")
            newReplace == oldReplace
        }else{
            false
        }
        isDeleteNew = deleteNew
        return deleteNew
    }
}