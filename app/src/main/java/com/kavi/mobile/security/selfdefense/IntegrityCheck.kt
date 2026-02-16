package com.kavi.mobile.security.selfdefense

import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Build
import android.util.Base64
import android.util.Log
import java.security.MessageDigest

/**
 * Integrity Check - Verifies Kavi hasn't been tampered with
 * Checks:
 * - APK Signature (detects modification)
 * - Installer verification (optional)
 */
class IntegrityCheck(private val context: Context) {

    companion object {
        private const val TAG = "IntegrityCheck"
        
        // This should be your actual release key hash
        // For debug, it will be the debug key hash.
        private const val EXPECTED_SIGNATURE_HASH = "" // Add real hash here later
    }

    /**
     * Check if the app signature matches expected value
     */
    fun isSignatureValid(): Boolean {
        try {
            val currentSignature = getSignature() ?: return false
            
            // If we haven't set an expected hash yet, just return true (Dev mode)
            // But log the hash so we can find it
            if (EXPECTED_SIGNATURE_HASH.isEmpty()) {
                Log.d(TAG, "Current Signature Checksum: $currentSignature")
                return true
            }
            
            return currentSignature == EXPECTED_SIGNATURE_HASH
        } catch (e: Exception) {
            Log.e(TAG, "Integrity check failed", e)
            return false
        }
    }

    @Suppress("DEPRECATION")
    private fun getSignature(): String? {
        try {
            val pm = context.packageManager
            val packageName = context.packageName
            
            val signature = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val packageInfo = pm.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES)
                packageInfo.signingInfo.apkContentsSigners[0]
            } else {
                val packageInfo = pm.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
                packageInfo.signatures[0]
            }

            val md = MessageDigest.getInstance("SHA-256")
            md.update(signature.toByteArray())
            return Base64.encodeToString(md.digest(), Base64.NO_WRAP)
        } catch (e: Exception) {
            return null
        }
    }
}
