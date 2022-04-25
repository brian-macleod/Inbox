package com.github.macleod.inbox.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.paging.PagingData
import androidx.paging.map
import com.github.macleod.inbox.data.model.*
import com.github.macleod.inbox.data.repository.ContactRepository
import com.github.macleod.inbox.data.repository.MessageRepository
import com.github.macleod.inbox.data.repository.ThreadRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ConversationViewModel(private val threadRepository: ThreadRepository,
                            private val contactRepository: ContactRepository,
                            private val messageRepository: MessageRepository
): ViewModel()
{
    class Factory(private val threadRepository: ThreadRepository,
                  private val contactRepository: ContactRepository,
                  private val messageRepository: MessageRepository
    ): ViewModelProvider.Factory
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
            return ConversationViewModel(threadRepository, contactRepository, messageRepository) as T
        }

    }

    /**
     * Get messages
     *
     * @param threadID
     * @return
     */
    suspend fun getMessages(threadID: Long): Flow<PagingData<Message>>
    {
        return messageRepository.getMessageData(threadID).map { pagingData ->
            pagingData.map { getMessage(it) }
        }
    }

    /**
     * Get message
     *
     * @param messageData
     * @return
     */
    private suspend fun getMessage(messageData: MessageData): Message
    {
        val contact = contactRepository.getContactForAddress(messageData.address)
        return if (messageData is SMSMessageData)
        {
            SMSMessage(messageData, contact)
        }
        else
        {
            MMSMessage(messageData as MMSMessageData, contact)
        }
    }
}