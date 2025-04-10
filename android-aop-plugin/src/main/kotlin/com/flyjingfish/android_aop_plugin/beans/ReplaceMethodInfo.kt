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
            isDeleteNew(newMethodDesc,oldMethodDesc)
        }else{
            false
        }
        isDeleteNew = deleteNew
        return deleteNew
    }

    companion object{
        fun isDeleteNew(newMethodDesc:String,oldMethodDesc:String):Boolean{
            val newTypes = Type.getArgumentTypes(newMethodDesc)
            val oldTypes = Type.getArgumentTypes(oldMethodDesc)
            return if (newTypes.isNotEmpty()){
                newTypes[0].className == Class::class.java.name && newTypes.toList().subList(1,newTypes.size).joinToString("") == oldTypes.joinToString("")
//                newTypes[newTypes.size - 1].className == Class::class.java.name && newTypes.toList().subList(0,newTypes.size-1).joinToString("") == oldTypes.joinToString("")
            }else{
                false
            }
        }
    }
}