package com.example.emergencynetwork.util

import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

object AESHelper {

    private const val SECRET = "1234567890123456"  // 16-char static key for Phase 1

    fun encrypt(input: String): String {
        val cipher = Cipher.getInstance("AES")
        val key = SecretKeySpec(SECRET.toByteArray(), "AES")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val enc = cipher.doFinal(input.toByteArray())
        return Base64.encodeToString(enc, Base64.DEFAULT)
    }

    fun decrypt(input: String): String {
        val cipher = Cipher.getInstance("AES")
        val key = SecretKeySpec(SECRET.toByteArray(), "AES")
        cipher.init(Cipher.DECRYPT_MODE, key)
        val decoded = Base64.decode(input, Base64.DEFAULT)
        return String(cipher.doFinal(decoded))
    }
}
