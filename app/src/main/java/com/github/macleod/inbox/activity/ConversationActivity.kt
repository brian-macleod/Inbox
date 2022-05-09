package com.github.macleod.inbox.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.trimmedLength
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.github.macleod.inbox.R
import com.github.macleod.inbox.adapter.ConversationAdapter
import com.github.macleod.inbox.data.model.Contact
import com.github.macleod.inbox.data.model.Conversation
import com.github.macleod.inbox.data.model.ImageAttachment
import com.github.macleod.inbox.data.model.MMSMessage
import com.github.macleod.inbox.data.repository.ContactRepository
import com.github.macleod.inbox.data.repository.MessageRepository
import com.github.macleod.inbox.data.repository.ThreadRepository
import com.github.macleod.inbox.viewmodel.ConversationViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ConversationActivity : AppCompatActivity()
{
    companion object
    {
        const val EXTRA_CONVERSATION_ID = "com.github.macleod.inbox.activity.ConversationActivity.EXTRA_CONVERSATION_ID"
        const val EXTRA_CONVERSATION_CONTACTS = "com.github.macleod.inbox.activity.ConversationActivity.EXTRA_CONTACTS"

        const val ENABLED_ALPHA = 0xFF
        const val DISABLED_ALPHA = 0x41
    }

    private val messageTextItem: EditText by lazy { findViewById<EditText>(R.id.conversation_message_text_item) }
    private val sendButton: ImageButton by lazy { findViewById<ImageButton>(R.id.conversation_send_button) }
    private var conversationID: Long = -1
    private lateinit var conversationAdapter: ConversationAdapter

    /**
     * On create
     *
     * @param savedInstanceState
     */
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversation)

        conversationID = intent.getLongExtra(EXTRA_CONVERSATION_ID, -1)
        val contacts = intent.getSerializableExtra(EXTRA_CONVERSATION_CONTACTS) as List<Contact>

        if (conversationID > 0)
        {
            populateView(conversationID, contacts)
        }

        bindHandlers()
    }

    /**
     * Finish
     *
     */
    override fun finish()
    {
        super.finish()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    /**
     * On options item selected
     *
     * @param item
     * @return
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean
    {
        return when (item.itemId)
        {
            android.R.id.home ->
            {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Bind handlers
     *
     */
    private fun bindHandlers()
    {
        setSendButtonEnabled(false)

        /*
         * Message Text Changed
         */
        messageTextItem.addTextChangedListener {
            val text = it.toString()
            val isEnabled = text.trimmedLength() > 0
            setSendButtonEnabled(isEnabled)
        }

        /*
         * Send Button Clicked
         */
        sendButton.setOnClickListener {
            Log.i("MainActivity", "SEND CLICKED!!!!!!! --> " + System.currentTimeMillis()) // TODO:
            messageTextItem.text.clear()
            closeKeyboard()
        }

        /*
         * Message Clicked
         */
        conversationAdapter.onItemClick = { message, offset ->
            if (message is MMSMessage)
            {
                val attachment = message.attachments[offset]
                if (attachment is ImageAttachment)
                {
                    startImageActivity(attachment.id)
                }
            }
        }
    }

    /**
     * Set send button enabled
     *
     * @param isEnabled
     */
    private fun setSendButtonEnabled(isEnabled: Boolean)
    {
        sendButton.isEnabled = isEnabled
        sendButton.imageAlpha = if (isEnabled) ENABLED_ALPHA else DISABLED_ALPHA
    }

    /**
     * Close keyboard
     *
     */
    private fun closeKeyboard()
    {
        if (currentFocus != null)
        {
            val token = currentFocus?.windowToken
            val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(token, 0)
        }
    }

    /**
     * Start image activity
     *
     * @param partID
     */
    private fun startImageActivity(partID: Long)
    {
        val intent = Intent(this, ImageActivity::class.java).apply {
            putExtra(ImageActivity.EXTRA_CONVERSATION_ID, conversationID)
            putExtra(ImageActivity.EXTRA_PART_ID, partID)
        }
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    /**
     * Populate view
     *
     * @param conversationID
     * @param contacts
     */
    private fun populateView(conversationID: Long, contacts: List<Contact>)
    {
        val threadRepository = ThreadRepository()
        val contactRepository = ContactRepository()
        val messageRepository = MessageRepository()
        val viewModelFactory = ConversationViewModel.Factory(threadRepository, contactRepository, messageRepository)
        val viewModel = ViewModelProvider(this, viewModelFactory).get(ConversationViewModel::class.java)

        conversationAdapter = ConversationAdapter(conversationID, contacts)

        setSupportActionBar(findViewById(R.id.conversation_toolbar))
        supportActionBar?.title = Conversation.getParticipantLabel(contacts)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        val recyclerView = findViewById<RecyclerView>(R.id.conversation_message_list)
        recyclerView.adapter = conversationAdapter

        CoroutineScope(Dispatchers.IO).launch {
            viewModel.getMessages(conversationID).collectLatest { conversations ->
                conversationAdapter.submitData(conversations)
            }
        }
    }
}