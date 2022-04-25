package com.github.macleod.inbox.data.model

data class Conversation(val id: Long, val date: Long, val contacts: List<Contact>?, val messageCount: Int, val lastMessage: Message?)
{
    companion object
    {
        /**
         * Get participant label
         *
         * @param contacts
         * @return
         */
        fun getParticipantLabel(contacts: List<Contact>?): String
        {
            val sortedList = contacts?.sortedBy { it.displayName }

            val labelBuilder = StringBuilder()
            if (sortedList != null) // TODO: This should never be null but add logic here just in case
            {
                for (contact in sortedList)
                {
                    var displayName = contact.displayName
                    if (displayName == null)
                    {
                        displayName = contact.address
                    }
                    else if (contacts!!.size > 1)
                    {
                        val index = displayName.indexOf(" ")
                        if (index > 0)
                        {
                            displayName = displayName.substring(0, index)
                        }
                    }

                    if (labelBuilder.isNotEmpty())
                    {
                        labelBuilder.append(", ")
                    }
                    labelBuilder.append(displayName)
                }
            }
            return labelBuilder.toString()
        }
    }

    /**
     * Get participant label
     *
     * @return
     */
    fun getParticipantLabel(): String
    {
        return getParticipantLabel(contacts)
    }
}