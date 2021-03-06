package com.github.macleod.inbox.data.repository

import android.graphics.Bitmap
import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.github.macleod.inbox.data.model.*
import com.github.macleod.inbox.data.source.*
import com.github.macleod.inbox.data.source.ContactDataSource
import com.github.macleod.inbox.data.source.MMSDataSource
import com.github.macleod.inbox.data.source.SMSDataSource
import kotlinx.coroutines.flow.Flow

class MessageRepository
{
    /**
     * Get messages
     *
     * @param threadID
     * @return
     */
    suspend fun getMessageData(threadID: Long): Flow<PagingData<MessageData>>
    {
        val messageIDs = getMessageIDs(threadID)
        val config = PagingConfig(pageSize = MessagePagingSource.PAGE_SIZE,
                                  initialLoadSize = MessagePagingSource.INITIAL_LOAD_SIZE,
                                  prefetchDistance = MessagePagingSource.PREFETCH_DISTANCE,
                                  enablePlaceholders = false)
        val pager = Pager(
            config = config,
            pagingSourceFactory = { MessagePagingSource(threadID,  messageIDs) }
        )
        return pager.flow
    }

    /**
     * Get last message
     *
     * @param threadID
     * @return
     */
    suspend fun getLastMessage(threadID: Long): Message?
    {
        val smsDataSource = SMSDataSource()
        val mmsDataSource = MMSDataSource()
        val contactDataSource = ContactDataSource()

        val smsMessageData = smsDataSource.getDataForLastMessage(threadID)
        val mmsMessageData = mmsDataSource.getDataForLastMessage(threadID)

        return if (smsMessageData != null && (mmsMessageData == null || smsMessageData.dateReceived > mmsMessageData.dateReceived))
        {
            val contact = contactDataSource.getContactForAddress(smsMessageData.address)
            SMSMessage(smsMessageData, contact)
        }
        else if (mmsMessageData != null)
        {
            val contact = contactDataSource.getContactForAddress(mmsMessageData.address)
            MMSMessage(mmsMessageData, contact)
        }
        else
        {
            null
        }
    }

    /**
     * Get image IDs
     *
     * @param threadID
     * @return
     */
    suspend fun getImageIDs(threadID: Long): LongArray
    {
        val mmsDataSource = MMSDataSource()

        val messageIDPairs = mmsDataSource.getMessageIDs(threadID)
        val messageIDs = messageIDPairs.map { it.first }

        return mmsDataSource.getPartsForMessages(ContentType.IMAGE, *messageIDs.toLongArray())
    }

    /**
     * Get images
     *
     * @param partIDs
     * @param startingIndex
     * @return
     */
    suspend fun getImages(partIDs: LongArray, startingIndex: Int): Flow<PagingData<Bitmap>>
    {
        val config = PagingConfig(pageSize = ImagePagingSource.PAGE_SIZE,
                                  initialLoadSize = ImagePagingSource.INITIAL_LOAD_SIZE,
                                  prefetchDistance = ImagePagingSource.PREFETCH_DISTANCE,
                                  enablePlaceholders = false)
        val pager = Pager(
            config = config,
            pagingSourceFactory = { ImagePagingSource(partIDs, startingIndex) }
        )
        return pager.flow
    }

    /**
     * Get message IDs
     *
     * @param threadID
     * @return
     */
    private suspend fun getMessageIDs(threadID: Long): LongArray
    {
        val smsDataSource = SMSDataSource()
        val mmsDataSource = MMSDataSource()

        var idPairs = smsDataSource.getMessageIDs(threadID)
        idPairs += mmsDataSource.getMessageIDs(threadID)
        idPairs = idPairs.sortedByDescending { it.second }

        val ret = idPairs.map { it.first }
        return ret.toLongArray()
    }
}