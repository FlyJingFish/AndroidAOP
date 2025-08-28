package com.flyjingfish.android_aop_plugin.utils

import java.io.File

object AppClasses {
    private val jars = mutableSetOf<String>()
    private val files = mutableSetOf<String>()
    private val moduleNames = mutableSetOf<String>()
    fun addClasses(jars:List<File>, files:List<File>){
        this.jars.clear()
        this.files.clear()
        for (file in jars) {
            this.jars.add(file.absolutePath)
        }
        for (file in files) {
            this.files.add(file.absolutePath)
        }
    }
    fun putAllClasses(jars: MutableList<File>, files:MutableList<File>){
        jars.addAll(this.jars.map { File(it) })
        files.addAll(this.files.map { File(it) })
    }

    fun addModuleName(moduleName: String){
        moduleNames.add(moduleName)
    }

    fun clearModuleNames(){
        moduleNames.clear()
    }

    fun getAllModuleNames(): Set<String>{
        return moduleNames
    }
}