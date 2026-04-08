package com.megabot.engine.api

import android.util.Base64
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Cryptography/hashing API exposed to JavaScript scripts.
 */
class SecurityApi {

    fun hashCode(value: String): Int = value.hashCode()

    fun sha(value: String): String = digest("SHA-1", value)
    fun sha256(value: String): String = digest("SHA-256", value)
    fun sha384(value: String): String = digest("SHA-384", value)
    fun sha512(value: String): String = digest("SHA-512", value)
    fun md5(value: String): String = digest("MD5", value)

    fun base64Encode(value: String): String {
        return Base64.encodeToString(value.toByteArray(), Base64.NO_WRAP)
    }

    fun base64Decode(value: String): String {
        return String(Base64.decode(value, Base64.NO_WRAP))
    }

    fun aesEncode(key: String, iv: String, value: String): String {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val keySpec = SecretKeySpec(key.toByteArray().copyOf(16), "AES")
        val ivSpec = IvParameterSpec(iv.toByteArray().copyOf(16))
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)
        val encrypted = cipher.doFinal(value.toByteArray())
        return Base64.encodeToString(encrypted, Base64.NO_WRAP)
    }

    fun aesDecode(key: String, iv: String, value: String): String {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val keySpec = SecretKeySpec(key.toByteArray().copyOf(16), "AES")
        val ivSpec = IvParameterSpec(iv.toByteArray().copyOf(16))
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)
        val decrypted = cipher.doFinal(Base64.decode(value, Base64.NO_WRAP))
        return String(decrypted)
    }

    private fun digest(algorithm: String, value: String): String {
        val md = MessageDigest.getInstance(algorithm)
        val bytes = md.digest(value.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
