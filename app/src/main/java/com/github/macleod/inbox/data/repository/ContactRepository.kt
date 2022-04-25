package com.github.macleod.inbox.data.repository

import com.github.macleod.inbox.data.model.Contact
import com.github.macleod.inbox.data.source.ContactDataSource

/**
 * Contact repository
 *
 * @constructor Create empty Contact repository
 */
class ContactRepository
{
    /**
     * Get contact for recipient
     *
     * @param recipientID
     * @return
     */
    suspend fun getContactForRecipient(recipientID: Long): Contact?
    {
        val contactDataSource = ContactDataSource()
        return contactDataSource.getContactForRecipient(recipientID)
    }

    /**
     * Get contact for address
     *
     * @param address
     * @return
     */
    suspend fun getContactForAddress(address: String): Contact
    {
        val contactDataSource = ContactDataSource()
        return contactDataSource.getContactForAddress(address)
    }
}