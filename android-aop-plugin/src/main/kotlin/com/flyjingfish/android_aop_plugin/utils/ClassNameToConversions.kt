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
        returnToValue["I"] = "intValue(%1\$s)"
        returnToValue["S"] = "shortValue(%1\$s)"
        returnToValue["B"] = "byteValue(%1\$s)"
        returnToValue["C"] = "charValue(%1\$s)"
        returnToValue["J"] = "longValue(%1\$s)"
        returnToValue["F"] = "floatValue(%1\$s)"
        returnToValue["D"] = "doubleValue(%1\$s)"
        returnToValue["Z"] = "booleanValue(%1\$s)"
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
        value = if (value == null) {
            "return %1\$s"
        } else {
            "return Conversions.$value"
        }
        return value
    }
}