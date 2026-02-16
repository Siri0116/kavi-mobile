package com.kavi.mobile.automation

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat

/**
 * Phone Controller - Handles phone calls and SMS
 */
class PhoneController(private val context: Context) {

    companion object {
        private const val TAG = "PhoneController"
    }

    /**
     * Make a phone call to a contact by name
     */
    fun makeCall(contactName: String) {
        if (!hasCallPermission()) {
            Toast.makeText(context, "Call permission not granted", Toast.LENGTH_SHORT).show()
            return
        }

        val phoneNumber = lookupContact(contactName)
        
        if (phoneNumber != null) {
            makeCallToNumber(phoneNumber, contactName)
        } else {
            Toast.makeText(context, "Contact '$contactName' not found", Toast.LENGTH_SHORT).show()
            Log.w(TAG, "Contact not found: $contactName")
        }
    }

    /**
     * Make a call to a specific phone number
     */
    fun makeCallToNumber(phoneNumber: String, displayName: String = phoneNumber) {
        if (!hasCallPermission()) {
            Toast.makeText(context, "Call permission not granted", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val intent = Intent(Intent.ACTION_CALL)
            intent.data = Uri.parse("tel:$phoneNumber")
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            
            Toast.makeText(context, "Calling $displayName", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "Initiated call to: $phoneNumber")
        } catch (e: Exception) {
            Log.e(TAG, "Error making call", e)
            Toast.makeText(context, "Could not make call", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Lookup a contact's phone number by name
     */
    private fun lookupContact(contactName: String): String? {
        if (!hasContactsPermission()) {
            return null
        }

        var cursor: Cursor? = null
        try {
            val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
            val projection = arrayOf(
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
            )
            
            val selection = "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ?"
            val selectionArgs = arrayOf("%$contactName%")
            
            cursor = context.contentResolver.query(
                uri,
                projection,
                selection,
                selectionArgs,
                null
            )
            
            if (cursor != null && cursor.moveToFirst()) {
                val numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                if (numberIndex >= 0) {
                    val phoneNumber = cursor.getString(numberIndex)
                    Log.d(TAG, "Found contact: $contactName -> $phoneNumber")
                    return phoneNumber
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error looking up contact", e)
        } finally {
            cursor?.close()
        }
        
        return null
    }

    /**
     * Open the dialer with a pre-filled number
     */
    fun openDialer(phoneNumber: String = "") {
        try {
            val intent = Intent(Intent.ACTION_DIAL)
            if (phoneNumber.isNotEmpty()) {
                intent.data = Uri.parse("tel:$phoneNumber")
            }
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            
            Toast.makeText(context, "Opening dialer", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "Error opening dialer", e)
            Toast.makeText(context, "Could not open dialer", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Send SMS to a contact
     */
    fun sendSMS(contactName: String, message: String) {
        val phoneNumber = lookupContact(contactName)
        
        if (phoneNumber != null) {
            sendSMSToNumber(phoneNumber, message)
        } else {
            Toast.makeText(context, "Contact '$contactName' not found", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Send SMS to a phone number
     */
    fun sendSMSToNumber(phoneNumber: String, message: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("sms:$phoneNumber")
            intent.putExtra("sms_body", message)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            
            Toast.makeText(context, "Opening messages", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "Error sending SMS", e)
            Toast.makeText(context, "Could not open messaging app", Toast.LENGTH_SHORT).show()
        }
    }

    private fun hasCallPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun hasContactsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    }
}
