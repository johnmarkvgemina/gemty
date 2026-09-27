package com.example.util

import java.security.MessageDigest

object CryptoUtils {

    fun sha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun generateShortHash(input: String): String {
        val full = sha256(input)
        return full.take(12).uppercase()
    }
}
