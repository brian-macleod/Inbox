package com.github.macleod.inbox.data.source

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.github.macleod.inbox.data.model.MessageData

class MessagePagingSource(private val threadID: Long, private val messageIDs: LongArray): PagingSource<Int, MessageData>()
{
    /**
     * Companion
     *
     * @constructor Create empty Companion
     */
    companion object
    {
        const val INITIAL_LOAD_SIZE = 1
        const val PAGE_SIZE = 10
        const val PREFETCH_DISTANCE = PAGE_SIZE * 5
        private const val STARTING_INDEX = 0
    }

    /**
     * Loading API for [PagingSource].
     *
     * Implement this method to trigger your async load (e.g. from database or network).
     */
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, MessageData>
    {
        val pageIndex = params.key ?: STARTING_INDEX
        val loadSize = params.loadSize

        // Get the IDs of the messages in the requested range
        val endIndex = if ((pageIndex + loadSize) < messageIDs.size)
        {
            pageIndex + loadSize
        }
        else
        {
            messageIDs.size
        }

        val pageIDs = messageIDs.copyOfRange(pageIndex, endIndex)

        // Get the data for any SMS messages with the specified IDs
        val smsDataSource = SMSDataSource()
        val smsMessageData = smsDataSource.getMessageData(threadID, *pageIDs)

        // Get the data for any MMS messages with the specified IDs
        val mmsDataSource = MMSDataSource()
        val mmsMessageData = mmsDataSource.getMessageData(threadID, *pageIDs)

        // Combine the two lists of message data
        val messageDataList = mutableListOf<MessageData>()
        messageDataList.addAll(smsMessageData)
        messageDataList.addAll(mmsMessageData)
        messageDataList.sortByDescending { it.dateReceived }

        val prevKey = if (pageIndex == STARTING_INDEX) null else pageIndex - 1 // TODO: This is most likely wrong
        val nextKey =  if (messageDataList.isEmpty() || endIndex == messageIDs.size)
        {
            null
        }
        else
        {
            pageIndex + loadSize
        }

        return LoadResult.Page(
            data = messageDataList,
            prevKey = prevKey,
            nextKey = nextKey
        )
    }

    /**
     * Provide a [Key] used for the initial [load] for the next [PagingSource] due to invalidation
     * of this [PagingSource]. The [Key] is provided to [load] via [LoadParams.key].
     *
     * The [Key] returned by this method should cause [load] to load enough items to
     * fill the viewport around the last accessed position, allowing the next generation to
     * transparently animate in. The last accessed position can be retrieved via
     * [state.anchorPosition][PagingState.anchorPosition], which is typically
     * the top-most or bottom-most item in the viewport due to access being triggered by binding
     * items as they scroll into view.
     *
     * For example, if items are loaded based on integer position keys, you can return
     * [state.anchorPosition][PagingState.anchorPosition].
     *
     * Alternately, if items contain a key used to load, get the key from the item in the page at
     * index [state.anchorPosition][PagingState.anchorPosition].
     *
     * @param state [PagingState] of the currently fetched data, which includes the most recently
     * accessed position in the list via [PagingState.anchorPosition].
     *
     * @return [Key] passed to [load] after invalidation used for initial load of the next
     * generation. The [Key] returned by [getRefreshKey] should load pages centered around
     * user's current viewport. If the correct [Key] cannot be determined, `null` can be returned
     * to allow [load] decide what default key to use.
     */
    override fun getRefreshKey(state: PagingState<Int, MessageData>): Int?
    {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1) ?:
            state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}