package com.github.macleod.inbox.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.paging.PagingData
import androidx.paging.map
import com.github.macleod.inbox.data.repository.ContactRepository
import com.github.macleod.inbox.data.repository.MessageRepository
import com.github.macleod.inbox.data.repository.ThreadRepository
import com.github.macleod.inbox.data.model.Thread
import com.github.macleod.inbox.data.model.Contact
import com.github.macleod.inbox.data.model.Conversation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class InboxViewModel(private val threadRepository: ThreadRepository,
                     private val messageRepository: MessageRepository,
                     private val contactRepository: ContactRepository
): ViewModel()
{
    /**
     * Factory
     *
     * @property threadRepository
     * @property messageRepository
     * @property contactRepository
     * @constructor Create empty Factory
     */
    class Factory(private val threadRepository: ThreadRepository,
                  private val messageRepository: MessageRepository,
                  private val contactRepository: ContactRepository
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
            return InboxViewModel(threadRepository, messageRepository, contactRepository) as T
        }

    }
    /**
     * Get conversations
     *
     * @return
     */
    suspend fun getConversations(): Flow<PagingData<Conversation>>
    {
        return threadRepository.getThreads().map { pagingData ->
                    pagingData.map { getConversation(it) }
        }
    }

    /**
     * Get conversations
     *
     * @param thread
     * @return
     */
    private suspend fun getConversation(thread: Thread): Conversation // TODO: This function is way too slow
    {
        val contactList = mutableListOf<Contact>()
        for (recipientID in thread.recipients)
        {
            val contact = contactRepository.getContactForRecipient(recipientID)
            if (contact != null) // TODO: Handle this
            {
                contactList.add(contact)
            }
        }

        val lastMessage = messageRepository.getLastMessage(thread.id)
        return Conversation(thread.id, thread.date, contactList, thread.messageCount, lastMessage)
    }
}