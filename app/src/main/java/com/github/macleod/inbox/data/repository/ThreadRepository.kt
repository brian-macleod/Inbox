package com.github.macleod.inbox.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.github.macleod.inbox.data.model.Thread
import com.github.macleod.inbox.data.source.ThreadPagingSource
import kotlinx.coroutines.flow.Flow

class ThreadRepository
{
    /**
     * Get thread flow
     *
     * @return
     */
    suspend fun getThreads(): Flow<PagingData<Thread>>
    {
        val config = PagingConfig(pageSize = ThreadPagingSource.PAGE_SIZE,
                                  initialLoadSize = ThreadPagingSource.INITIAL_LOAD_SIZE,
                                  prefetchDistance = ThreadPagingSource.PREFETCH_DISTANCE,
                                  enablePlaceholders = false)
        val pager = Pager(
            config = config,
            pagingSourceFactory = { ThreadPagingSource() }
        )
        return pager.flow
    }
}