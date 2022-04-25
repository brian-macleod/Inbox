package com.github.macleod.inbox.data.source

import android.content.ContentResolver
import android.net.Uri
import android.provider.ContactsContract.PhoneLookup
import com.github.macleod.inbox.InboxApp
import com.github.macleod.inbox.data.model.Contact

/**
 * Contact data source
 *
 * @constructor Create empty Contact data source
 */
internal class ContactDataSource
{
    companion object
    {
        private const val CANONICAL_ADDRESS_CONTENT_URI = "content://mms-sms/canonical-addresses"
        private const val CANONICAL_ADDRESS_ADDRESS     = "address"

        private val cachedRecipientMap = mutableMapOf<Long, Contact>() // Map<RecipientID, Contact>
    }

    private val contentResolver: ContentResolver

    /**
     *
     */
    init
    {
        val context = InboxApp.context
        contentResolver = context.contentResolver
    }

    /**
     * Get contact for recipient
     *
     * @param recipientID
     * @return
     */
    suspend fun getContactForRecipient(recipientID: Long): Contact?
    {
        var contact = cachedRecipientMap[recipientID]
        if (contact != null)
        {
            return contact
        }

        val address = getAddressForRecipient(recipientID) ?: return null
        contact = getContactForAddress(address) ?: return null
        cachedRecipientMap[recipientID] = contact
        return contact
    }

    /**
     * Get contact for address
     *
     * @param address
     * @return
     */
    suspend fun getContactForAddress(address: String): Contact
    {
        val projection = arrayOf(PhoneLookup._ID,
                                 PhoneLookup.DISPLAY_NAME)
        val uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(address))
        contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            if (cursor != null)
            {
                val contactIDIndex = cursor.getColumnIndex(PhoneLookup._ID)
                val displayNameIndex = cursor.getColumnIndex(PhoneLookup.DISPLAY_NAME)
                if (cursor.moveToFirst())
                {
                    val contactID = cursor.getLong(contactIDIndex)
                    val displayName = cursor.getString(displayNameIndex)
                    return Contact(contactID, address, displayName)
                }
            }
        }
        return Contact(null, address, null)
    }

    /**
     * Get address for recipient
     *
     * @param recipientID
     * @return
     */
    private suspend fun getAddressForRecipient(recipientID: Long): String?
    {
        var uri = Uri.parse(CANONICAL_ADDRESS_CONTENT_URI)
        contentResolver.query(uri, null, "_id = $recipientID", null, null)?.use { cursor ->
            if (cursor != null)
            {
                val addressIndex = cursor.getColumnIndex(CANONICAL_ADDRESS_ADDRESS)
                if (cursor.moveToFirst())
                {
                    return cursor.getString(addressIndex)
                }
            }
        }
        return null
    }
}