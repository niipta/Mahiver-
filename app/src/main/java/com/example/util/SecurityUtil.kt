package com.example.util

import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Passphrase-based encryption utility for backup files.
 *
 * Unlike the old AndroidKeystore approach (which was device-locked and made
 * backups useless after uninstall), this uses PBKDF2 to derive an AES key
 * from a user-provided passphrase. The encrypted backup can be decrypted on
 * ANY device that knows the passphrase — making cross-device restore work.
 *
 * Format: [salt(16)][iv(12)][ciphertext+tag]
 * All Base64-encoded as a single string for easy JSON embedding.
 */
object SecurityUtil {

    private const val PBKDF2_ITERATIONS = 100_000
    private const val KEY_LENGTH_BITS = 256
    private const val SALT_LENGTH = 16
    private const val IV_LENGTH = 12
    private const val GCM_TAG_BITS = 128

    /**
     * Derives an AES-256 key from a passphrase + salt using PBKDF2.
     * This is deliberately slow (100K iterations) to resist brute force.
     */
    private fun deriveKey(passphrase: String, salt: ByteArray): SecretKey {
        val spec = PBEKeySpec(passphrase.toCharArray(), salt, PBKDF2_ITERATIONS, KEY_LENGTH_BITS)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val keyBytes = factory.generateSecret(spec).encoded
        return SecretKeySpec(keyBytes, "AES")
    }

    /**
     * Encrypts plain text using a passphrase.
     * Returns Base64(salt + iv + ciphertext).
     */
    fun encryptData(plainText: String, passphrase: String = DEFAULT_PASSPHRASE): String {
        val salt = ByteArray(SALT_LENGTH).also { SecureRandom().nextBytes(it) }
        val key = deriveKey(passphrase, salt)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val iv = cipher.iv
        val encrypted = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

        // Combine salt + iv + ciphertext
        val combined = ByteArray(SALT_LENGTH + IV_LENGTH + encrypted.size)
        System.arraycopy(salt, 0, combined, 0, SALT_LENGTH)
        System.arraycopy(iv, 0, combined, SALT_LENGTH, IV_LENGTH)
        System.arraycopy(encrypted, 0, combined, SALT_LENGTH + IV_LENGTH, encrypted.size)

        return android.util.Base64.encodeToString(combined, android.util.Base64.NO_WRAP)
    }

    /**
     * Decrypts Base64(salt + iv + ciphertext) using a passphrase.
     * Throws IllegalArgumentException on wrong passphrase or corrupt data.
     */
    fun decryptData(encryptedBase64: String, passphrase: String = DEFAULT_PASSPHRASE): String {
        try {
            val combined = android.util.Base64.decode(encryptedBase64, android.util.Base64.NO_WRAP)
            if (combined.size < SALT_LENGTH + IV_LENGTH) return ""

            val salt = combined.copyOfRange(0, SALT_LENGTH)
            val iv = combined.copyOfRange(SALT_LENGTH, SALT_LENGTH + IV_LENGTH)
            val encrypted = combined.copyOfRange(SALT_LENGTH + IV_LENGTH, combined.size)

            val key = deriveKey(passphrase, salt)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_BITS, iv))

            val decrypted = cipher.doFinal(encrypted)
            return String(decrypted, Charsets.UTF_8)
        } catch (e: javax.crypto.AEADBadTagException) {
            throw IllegalArgumentException("Wrong passphrase or corrupted backup file.", e)
        } catch (e: Exception) {
            throw IllegalArgumentException("Failed to decrypt backup data: ${e.message}", e)
        }
    }

    /**
     * Default passphrase for backward compatibility.
     *
     * NOTE: The old AndroidKeystore-based encryption was device-locked and
     * is NO LONGER USED. Backups are now created as plain JSON (portable)
     * by BackupRepository. This passphrase is only used if encryption is
     * explicitly requested in the future.
     */
    private const val DEFAULT_PASSPHRASE = "MahirVerse_v2_portable_backup"
}
