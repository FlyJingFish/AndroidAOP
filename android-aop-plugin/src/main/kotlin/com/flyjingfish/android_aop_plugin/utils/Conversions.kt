package com.flyjingfish.android_aop_plugin.utils


object Conversions {
    private val argsToObject: MutableMap<String, String> = HashMap()
    private val returnToValue: MutableMap<String, String> = HashMap()

    init {
        argsToObject["I"] = "intObject(%1\$s)"
        argsToObject["S"] = "shortObject(%1\$s)"
        argsToObject["B"] = "byteObject(%1\$s)"
        argsToObject["C"] = "charObject(%1\$s)"
        argsToObject["J"] = "longObject(%1\$s)"
        argsToObject["F"] = "floatObject(%1\$s)"
        argsToObject["D"] = "doubleObject(%1\$s)"
        argsToObject["Z"] = "booleanObject(%1\$s)"
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