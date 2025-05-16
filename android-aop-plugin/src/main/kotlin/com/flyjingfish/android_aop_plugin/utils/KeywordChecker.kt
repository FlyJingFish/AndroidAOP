package com.flyjingfish.android_aop_plugin.utils

object KeywordChecker {

    private val javaKeywords = listOf(
        // 基本控制结构
        "if", "else", "switch", "case", "default", "while", "do", "for", "break", "continue", "return",

        // 访问控制与类定义
        "class", "interface", "enum", "abstract", "extends", "implements", "final", "strictfp",

        // 变量与方法修饰
        "private", "protected", "public", "static", "transient", "volatile", "synchronized", "native",

        // 异常处理
        "try", "catch", "finally", "throw", "throws", "assert",

        // 对象与继承
        "super", "this", "new", "instanceof",

        // 类型相关
        "package", "import",

        // 保留字（未来可能使用）
        "const", "goto",

        // Java 9+ 模块系统关键字
        "open", "module", "requires", "exports", "provides", "uses", "to", "with", "transitive"
    )


    /**
     * 判断类名是否包含任意 Java 编译器关键字
     */
    fun containsKeywordAsWord(className: String): Boolean {
        return javaKeywords.any { keyword ->
            Regex("\\b$keyword\\b").containsMatchIn(className)
        }
    }

    fun getClass(className: String?):String{
        return if (className == null){
            "null"
        }else if (containsKeywordAsWord(className)){
            "Class.forName(\"$className\")"
        }else{
            "$className.class"
        }
    }

    fun getInstance(className: String,checkCast:String):String{
        return if (containsKeywordAsWord(className)){
            "(($checkCast)Class.forName(\"$className\").newInstance())"
        }else{
            "new $className()"
        }
    }
}


