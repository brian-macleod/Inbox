package com.github.macleod.inbox.data.source

import android.content.ContentResolver
import android.provider.Telephony.Sms
import com.github.macleod.inbox.InboxApp
import com.github.macleod.inbox.data.model.SMSMessageData
import com.github.macleod.inbox.data.model.MessageType

internal class SMSDataSource: MessageDataSource<SMSMessageData>
{
    /**
     * Get content resolver
     *
     * @return
     */
    private fun getContentResolver(): ContentResolver
    {
        val context = InboxApp.context
        return context.contentResolver
    }

    /**
     * Get message IDs
     *
     * @param threadID
     * @return
     */
    override suspend fun getMessageIDs(threadID: Long): List<Pair<Long, Long>>
    {
        val projection = arrayOf(Sms._ID, Sms.DATE)
        val selection = Sms.THREAD_ID + " = ?"
        val selectionArgs = arrayOf(threadID.toString())
        val sortOrder = Sms.DATE + " DESC"

        val idList = ArrayList<Pair<Long, Long>>()

        val contentResolver = getContentResolver()
        contentResolver.query(Sms.CONTENT_URI, projection, selection, selectionArgs, sortOrder)?.use { cursor ->
            val idIndex = cursor.getColumnIndex(Sms._ID)
            val dateIndex = cursor.getColumnIndex(Sms.DATE)

            while (cursor.moveToNext())
            {
                val id = cursor.getLong(idIndex)
                val date = cursor.getLong(dateIndex)

                val pair = Pair(id, date)
                idList.add(pair)
            }
        }
        return idList
    }

    /**
     * Get message descriptors
     *
     * @param threadID
     * @param messageIDs
     * @return
     */
    override suspend fun getMessageData(threadID: Long, vararg messageIDs: Long): List<SMSMessageData>
    {
        val messageDataList = mutableListOf<SMSMessageData>()
        if (messageIDs.isEmpty()) return messageDataList

        val projection = arrayOf(
            Sms._ID,
            Sms.BODY,
            Sms.DATE_SENT,
            Sms.DATE,
            Sms.ADDRESS,
            Sms.TYPE
        )

        val selectionArgList = mutableListOf<String>()
        selectionArgList.add(threadID.toString())

        val inListBuilder = StringBuilder()
        for (id in messageIDs)
        {
            if (inListBuilder.isNotEmpty())
            {
                inListBuilder.append(", ")
            }
            inListBuilder.append("?")
            selectionArgList.add(id.toString())
        }

        val selection =
            Sms.THREAD_ID + " = ? AND " + Sms._ID + " IN (" + inListBuilder.toString() + ")"
        val selectionArgs = selectionArgList.toTypedArray()
        val sortOrder = Sms.DATE + " DESC"

        val contentResolver = getContentResolver()
        contentResolver.query(Sms.CONTENT_URI, projection, selection, selectionArgs, sortOrder)
            ?.use { cursor ->
                val idIndex = cursor.getColumnIndex(Sms._ID)
                val bodyIndex = cursor.getColumnIndex(Sms.BODY)
                val dateSentIndex = cursor.getColumnIndex(Sms.DATE_SENT)
                val dateReceivedIndex = cursor.getColumnIndex(Sms.DATE)
                val addressIndex = cursor.getColumnIndex(Sms.ADDRESS)
                val typeIndex = cursor.getColumnIndex(Sms.TYPE)

                while (cursor.moveToNext())
                {
                    val id = cursor.getLong(idIndex)
                    val body = cursor.getString(bodyIndex)
                    val dateSent = cursor.getLong(dateSentIndex)
                    val dateReceived = cursor.getLong(dateReceivedIndex)
                    val address = cursor.getString(addressIndex)
                    val typeInt = cursor.getInt(typeIndex)
                    val type = MessageType.fromInt(typeInt) ?: MessageType.RECEIVED // TODO

                    val messageData =
                        SMSMessageData(id, body, dateSent, dateReceived, address, type)
                    messageDataList.add(messageData)
                }
            }
        return messageDataList
    }

    /**
     * Get last message
     *
     * @param threadID
     * @return
     */
    override suspend fun getDataForLastMessage(threadID: Long): SMSMessageData?
    {
        val projection = arrayOf(
            Sms._ID,
            Sms.BODY,
            Sms.DATE_SENT,
            Sms.DATE,
            Sms.ADDRESS,
            Sms.TYPE
        )
        val selection = Sms.THREAD_ID + " = ?"
        val selectionArgs = arrayOf(threadID.toString())
        val sortOrder = Sms.DATE + " DESC"

        val contentResolver = getContentResolver()
        contentResolver.query(Sms.CONTENT_URI, projection, selection, selectionArgs, sortOrder)?.use { cursor -> // TODO: Add a projection
            if (cursor != null)
            {
                val idIndex = cursor.getColumnIndex(Sms._ID)
                val bodyIndex = cursor.getColumnIndex(Sms.BODY)
                val dateSentIndex = cursor.getColumnIndex(Sms.DATE_SENT)
                val dateReceivedIndex = cursor.getColumnIndex(Sms.DATE)
                val addressIndex = cursor.getColumnIndex(Sms.ADDRESS)
                val typeIndex = cursor.getColumnIndex(Sms.TYPE)

                if (cursor.moveToFirst())
                {
                    val id = cursor.getLong(idIndex)
                    val body = cursor.getString(bodyIndex)
                    val dateSent = cursor.getLong(dateSentIndex)
                    val dateReceived = cursor.getLong(dateReceivedIndex)
                    val address = cursor.getString(addressIndex)
                    val typeInt = cursor.getInt(typeIndex)
                    val type = MessageType.fromInt(typeInt) ?: MessageType.RECEIVED // TODO

                    return SMSMessageData(id, body, dateSent, dateReceived, address, type)
                }
            }
        }
        return null
    }
}