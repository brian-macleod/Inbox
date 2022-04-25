package com.github.macleod.inbox.data.source

import android.content.ContentResolver
import android.provider.Telephony.Threads
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.github.macleod.inbox.InboxApp
import com.github.macleod.inbox.data.model.Thread

class ThreadPagingSource: PagingSource<Int, Thread>()
{
    private val contentResolver: ContentResolver

    companion object
    {
        const val INITIAL_LOAD_SIZE = 1
        const val PAGE_SIZE = 10
        const val PREFETCH_DISTANCE = PAGE_SIZE * 5
        private const val STARTING_INDEX = 0
    }

    /**
     *
     */
    init
    {
        val context = InboxApp.context
        contentResolver = context.contentResolver
    }

    /**
     * Loading API for [PagingSource].
     *
     * Implement this method to trigger your async load (e.g. from database or network).
     */
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Thread>
    {
        val pageIndex = params.key ?: STARTING_INDEX
        val loadSize = if (pageIndex < PAGE_SIZE)
        {
            INITIAL_LOAD_SIZE
        }
        else
        {
            params.loadSize
        }

        val baseURI = Threads.CONTENT_URI
        val uri = baseURI.buildUpon()
                         .appendQueryParameter("simple", "true")
                         .build()

        val projection = arrayOf(Threads._ID, Threads.DATE, Threads.RECIPIENT_IDS, Threads.MESSAGE_COUNT)
        val selection = Threads.MESSAGE_COUNT + " > 0"
        val sortOrder = Threads.DATE + " DESC"

        val threadList = mutableListOf<Thread>()

        contentResolver.query(uri, projection, selection, null, sortOrder)?.use { cursor ->
            if (cursor != null)
            {
                val idIndex = cursor.getColumnIndex(Threads._ID)
                val dateIndex = cursor.getColumnIndex(Threads.DATE)
                val recipientsIndex = cursor.getColumnIndex(Threads.RECIPIENT_IDS)
                val messageCountIndex = cursor.getColumnIndex(Threads.MESSAGE_COUNT)

                if (cursor.moveToPosition(pageIndex))
                {
                    var i = 0
                    do
                    {
                        val id = cursor.getLong(idIndex)
                        val date = cursor.getLong(dateIndex)
                        val recipientStr = cursor.getString(recipientsIndex)
                        val messageCount = cursor.getInt(messageCountIndex)

                        val recipientIDs = recipientStr.split(" ").map { it.toLong() }

                        val thread = Thread(id, date, recipientIDs, messageCount)
                        threadList.add(thread)
                    } while (++i < loadSize && cursor.moveToNext())
                }
            }
        }

        val prevKey = if (pageIndex == STARTING_INDEX) null else pageIndex - 1 // TODO: This is most likely wrong
        val nextKey = if (threadList.isEmpty()) null else pageIndex + loadSize

        return LoadResult.Page(
            data = threadList,
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
    override fun getRefreshKey(state: PagingState<Int, Thread>): Int?
    {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1) ?:
            state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    /**
     * Get thread
     *
     * @param id
     * @return
     */
    suspend fun getThread(id: Long): Thread?
    {
        val baseURI = Threads.CONTENT_URI
        val uri = baseURI.buildUpon()
            .appendQueryParameter("simple", "true")
            .build()

        val projection = arrayOf(Threads._ID,
                                 Threads.DATE,
                                 Threads.RECIPIENT_IDS,
                                 Threads.MESSAGE_COUNT)

        var selection = Threads._ID + " = ? AND " + Threads.MESSAGE_COUNT + " > 0"
        var selectionArgs = arrayOf(id.toString())

        contentResolver.query(uri, projection, selection, selectionArgs, null)?.use { cursor ->
            if (cursor != null)
            {
                val idIndex = cursor.getColumnIndex(Threads._ID)
                val dateIndex = cursor.getColumnIndex(Threads.DATE)
                val recipientsIndex = cursor.getColumnIndex(Threads.RECIPIENT_IDS)
                val messageCountIndex = cursor.getColumnIndex(Threads.MESSAGE_COUNT)

                if (cursor.moveToFirst())
                {
                    val id = cursor.getLong(idIndex)
                    val date = cursor.getLong(dateIndex)
                    val recipientStr = cursor.getString(recipientsIndex)
                    val messageCount = cursor.getInt(messageCountIndex)

                    val recipientIDs = recipientStr.split(" ").map { it.toLong() }

                    return Thread(id, date, recipientIDs, messageCount)
                }
            }
        }
        return null
    }
}