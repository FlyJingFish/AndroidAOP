package com.flyjingfish.android_aop_plugin.utils


object ClassNameToConversions {
    private val argsToObject: MutableMap<String, String> = HashMap()
    private val returnToValue: MutableMap<String, String> = HashMap()

    init {
        argsToObject["int"] = "intObject(%1\$s)"
        argsToObject["short"] = "shortObject(%1\$s)"
        argsToObject["byte"] = "byteObject(%1\$s)"
        argsToObject["char"] = "charObject(%1\$s)"
        argsToObject["long"] = "longObject(%1\$s)"
        argsToObject["float"] = "floatObject(%1\$s)"
        argsToObject["double"] = "doubleObject(%1\$s)"
        argsToObject["boolean"] = "booleanObject(%1\$s)"
        returnToValue["int"] = "intValue(%1\$s)"
        returnToValue["short"] = "shortValue(%1\$s)"
        returnToValue["byte"] = "byteValue(%1\$s)"
        returnToValue["char"] = "charValue(%1\$s)"
        returnToValue["long"] = "longValue(%1\$s)"
        returnToValue["float"] = "floatValue(%1\$s)"
        returnToValue["double"] = "doubleValue(%1\$s)"
        returnToValue["boolean"] = "booleanValue(%1\$s)"
        returnToValue["java.lang.String"] = "stringValue(%1\$s)"
    }

    fun getArgsXObject(key: String): String {
        var value = argsToObject[key]
        value = if (value == null) {
            "%1\$s"
        } else {
            "Conversions.$value"
        }
        return value
    }

    fun getReturnXObject(key: String): String {
        var value = returnToValue[key]
        value = if(key == "void"){
            "%1\$s"
        }else if (value == null) {
            "return %1\$s"
        } else {
            "return Conversions.$value"
        }
        return value
    }
}