package com.flyjingfish.android_aop_plugin.utils

import com.flyjingfish.android_aop_plugin.config.AndroidAopConfig
import java.security.MessageDigest


object FileHashUtils {
    private var fileHashMap1: HashMap<String, String> = HashMap()
    private var fileHashMap2: HashMap<String, String> = HashMap()
    private var fileHashMap3: HashMap<String, String> = HashMap()
    var isChangeAopMatch = true
    fun isAsmScan(file: String, fileBytes: ByteArray,step: Int): Boolean {
        if (!AndroidAopConfig.increment){
            return true
        }
        val oldHash = getFileHash(file,step)
        val hash = getSHA256Hash(fileBytes)
        WovenInfoUtils.removeClassCache(file)
        if (step == 2 && isChangeAopMatch){
//            printLog("isAsmScan1 = $file")
            putFileHash(file, fileBytes,step)
            return true
        }
        return if (oldHash != hash){
//            printLog("isAsmScan2 = $file")
            putFileHash(file, fileBytes,step)
            true
        }else{
            false
        }
    }

    fun overrideIsChange(file: String, fileBytes: ByteArray): Boolean {
        return isAsmScan(file, fileBytes,10)
    }

    fun recordOverrideChange(file: String, fileBytes: ByteArray) {
        if (!AndroidAopConfig.increment){
            return
        }
        putFileHash(file, fileBytes,10)
    }
    private fun putFileHash(file: String, fileBytes: ByteArray, step: Int) {
        val hash = getSHA256Hash(fileBytes)
        when(step){
            1 -> fileHashMap1[file] = hash
            2 -> fileHashMap2[file] = hash
            3 -> fileHashMap3[file] = hash
        }

    }

    private fun getFileHash(file: String, step: Int): String? {
        return when(step){
            1 -> fileHashMap1[file]
            2 -> fileHashMap2[file]
            else -> fileHashMap3[file]
        }
    }

    private fun getSHA256Hash(fileBytes: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(fileBytes)
        return bytesToHex(hashBytes)
    }

    private fun bytesToHex(hash: ByteArray): String {
        val hexString = StringBuilder(2 * hash.size)
        for (i in hash.indices) {
            val hex = Integer.toHexString(0xff and hash[i].toInt())
            if (hex.length == 1) {
                hexString.append('0')
            }
            hexString.append(hex)
        }
        return hexString.toString()
    }
}