package com.github.macleod.inbox.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.paging.PagingData
import com.github.macleod.inbox.data.model.ContentType
import com.github.macleod.inbox.data.repository.MessageRepository
import kotlinx.coroutines.flow.Flow

class ImageViewModel private constructor(private val messageRepository: MessageRepository): ViewModel()
{
    class Factory(private val messageRepository: MessageRepository): ViewModelProvider.Factory
    {
        /**
         * Creates a new instance of the given `Class`.
         *
         *
         *
         * @param modelClass a `Class` whose instance is requested
         * @param <T>        The type parameter for the ViewModel.
         * @return a newly created ViewModel
        </T> */
        override fun <T : ViewModel?> create(modelClass: Class<T>): T
        {
            return ImageViewModel(messageRepository) as T
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
        return messageRepository.getImageIDs(threadID)
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
        return messageRepository.getImages(partIDs, startingIndex)
    }
}