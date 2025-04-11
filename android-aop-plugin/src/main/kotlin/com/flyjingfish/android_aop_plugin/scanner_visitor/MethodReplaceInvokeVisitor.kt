package com.flyjingfish.android_aop_plugin.scanner_visitor

import com.flyjingfish.android_aop_plugin.beans.ReplaceMethodInfo
import com.flyjingfish.android_aop_plugin.utils.WovenInfoUtils
import com.flyjingfish.android_aop_plugin.utils.isHasMethodBody
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor

open class MethodReplaceInvokeVisitor(
    classVisitor: ClassVisitor,private val wovenClassWriterFlags:Int,private val wovenParsingOptions:Int
) : ReplaceBaseClassVisitor(classVisitor) {
    lateinit var className: String
    lateinit var superName: String
    var replaced = false
    val mDeleteNews = mutableMapOf<String,List<ReplaceMethodInfo>>()
    override fun visit(
        version: Int,
        access: Int,
        name: String,
        signature: String?,
        superName: String,
        interfaces: Array<out String>?
    ) {
        super.visit(version, access, name, signature, superName, interfaces)
        className = name
        this.superName = superName
    }
    override fun visitMethod(
        access: Int,
        name: String,
        descriptor: String,
        signature: String?,
        exceptions: Array<String?>?
    ): MethodVisitor? {
        var mv: MethodVisitor? = super.visitMethod(access, name, descriptor, signature, exceptions)

        if (mv != null && access.isHasMethodBody()) {
            mv = if (wovenParsingOptions != 0){
                MethodReplaceInvokeAdapter2(className,superName,access,name,descriptor,mv).apply {
                    utils.onResultListener = object : MethodReplaceInvokeAdapterUtils.OnResultListener{
                        override fun onBack() {
                            replaced = true
                        }

                        override fun onBack(delNews:List<ReplaceMethodInfo>) {
                            if (delNews.isNotEmpty()){
                                mDeleteNews["$name@$descriptor"] = delNews
                            }
                        }
                    }
                }
            }else{
                MethodReplaceInvokeAdapter(className,superName,access,name,descriptor,mv).apply {
                    utils.onResultListener = object : MethodReplaceInvokeAdapterUtils.OnResultListener{
                        override fun onBack() {
                            replaced = true
                        }

                        override fun onBack(delNews:List<ReplaceMethodInfo>) {
                            if (delNews.isNotEmpty()){
                                mDeleteNews["$name@$descriptor"] = delNews
                            }
                        }
                    }
                }

            }
        }
        return mv
    }

}