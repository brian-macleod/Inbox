package com.github.macleod.inbox.data.source;

import com.github.macleod.inbox.data.model.MessageData

internal interface MessageDataSource<T: MessageData>
{
    /**
     * Get message IDs
     *
     * @param threadID
     * @return
     */
    suspend fun getMessageIDs(threadID: Long): List<Pair<Long, Long>>

    /**
     * Get message descriptors
     *
     * @param threadID
     * @param messageIDs
     * @return
     */
    suspend fun getMessageData(threadID: Long, vararg messageIDs: Long): List<T>

    /**
     * Get last message
     *
     * @param threadID
     * @return
     */
    suspend fun getDataForLastMessage(threadID: Long): T?
}
