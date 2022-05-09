package com.github.macleod.inbox.data.source

import android.content.ContentResolver
import android.content.ContentUris
import android.graphics.Bitmap
import android.provider.Telephony
import android.util.Size
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.github.macleod.inbox.InboxApp
import java.util.*

class ImagePagingSource(private val partIDs: LongArray, private val startingIndex: Int = STARTING_INDEX): PagingSource<Int, Bitmap>()
{
    companion object
    {
        const val INITIAL_LOAD_SIZE = 1
        const val PAGE_SIZE = 1
        const val PREFETCH_DISTANCE = PAGE_SIZE// * 5
        private const val STARTING_INDEX = 0
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
     * Loading API for [PagingSource].
     *
     * Implement this method to trigger your async load (e.g. from database or network).
     */
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Bitmap>
    {
        val loadSize = params.loadSize
        val maxIndex = partIDs.size - loadSize
        val pageIndex = params.key ?: if (startingIndex > maxIndex)
        {
            maxIndex
        }
        else
        {
            startingIndex
        }

        val context = InboxApp.context
        val resources = context.resources
        val displayMetrics = resources.displayMetrics

        val imageList = mutableListOf<Bitmap>()
        var i = pageIndex

        while (i >= 0 && i < partIDs.size && imageList.size < loadSize)
        {
            val partID = partIDs[i]
            val partURI = ContentUris.withAppendedId(Telephony.Mms.Part.CONTENT_URI, partID)
            val targetWidth = displayMetrics.widthPixels

            val size = Size(targetWidth, targetWidth)
            val image = contentResolver.loadThumbnail(partURI, size, null)
            imageList.add(image)
            i++
        }

        val nextKey = if (pageIndex == 0)
        {
            null
        }
        else if ((pageIndex - loadSize) < 0)
        {
            0
        }
        else
        {
            pageIndex - loadSize
        }
        val prevKey = if (imageList.isEmpty()) null else pageIndex + loadSize


        return LoadResult.Page(
            data = imageList,
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
    override fun getRefreshKey(state: PagingState<Int, Bitmap>): Int?
    {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1) ?:
            state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}