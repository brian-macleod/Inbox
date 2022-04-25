package com.github.macleod.inbox.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.github.macleod.inbox.R
import com.github.macleod.inbox.data.repository.ContactRepository
import com.github.macleod.inbox.data.repository.MessageRepository
import com.github.macleod.inbox.data.repository.ThreadRepository
import com.github.macleod.inbox.adapter.InboxAdapter
import com.github.macleod.inbox.viewmodel.InboxViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


/**
 * TODO:
 *
 * @constructor Create empty Main activity
 */
class InboxActivity : AppCompatActivity()
{
    companion object
    {
        private const val TAG = "InboxActivity"
        private const val PERMISSIONS_REQUEST = 1
    }

    /**
     * TODO:
     *
     * @param savedInstanceState
     */
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inbox)

        setSupportActionBar(findViewById(R.id.inbox_toolbar))
        supportActionBar?.title = "Inbox"

        if (!hasPermissions())
        {
            if (shouldShowRequestPermissionRationale(Manifest.permission.READ_SMS))
            {
                Toast.makeText(this, "Please allow permission!!!", Toast.LENGTH_SHORT).show() // TODO: Change this to something a little more professional
            }

            val requestedPermissions = arrayOf(Manifest.permission.READ_SMS, Manifest.permission.READ_CONTACTS)
            requestPermissions(requestedPermissions, PERMISSIONS_REQUEST)
        }
        else
        {
            refreshConversationList()
        }
    }

    /**
     * TODO:
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray)
    {
        if (requestCode == PERMISSIONS_REQUEST)
        {
            if (grantResults.size == 2 && grantResults.all { it == PackageManager.PERMISSION_GRANTED})
            {
                refreshConversationList()
            }
            else
            {
                // TODO:
            }
//            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
//            {
//                Toast.makeText(this, "Read SMS permission granted", Toast.LENGTH_SHORT).show()
//                Log.i(TAG, "[1] - " + hasPermissions())
//                if (hasPermissions())
//                {
//                    refreshConversationList()
//                }
//            }
//            else
//            {
//                Toast.makeText(this, "Read SMS permission denied", Toast.LENGTH_SHORT).show()
//            }
        }
        else
        {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    /**
     * TODO:
     *
     * @return
     */
    private fun hasPermissions(): Boolean
    {
        val readSMSPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
        val readContactsPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
        return readSMSPermission == PackageManager.PERMISSION_GRANTED &&
               readContactsPermission == PackageManager.PERMISSION_GRANTED
    }

    /**
     * TODO:
     *
     */
    private fun refreshConversationList() // TODO: Reorganize this
    {
        val threadRepository = ThreadRepository()
        val messageRepository = MessageRepository()
        val contactRepository = ContactRepository()
        val viewModelFactory = InboxViewModel.Factory(threadRepository, messageRepository, contactRepository)
        val viewModel = ViewModelProvider(this, viewModelFactory).get(InboxViewModel::class.java)

        val inboxAdapter = InboxAdapter()
        inboxAdapter.onItemClick = { conversation ->
            val intent = Intent(this, ConversationActivity::class.java).apply {
                putExtra(ConversationActivity.EXTRA_CONVERSATION_ID, conversation.id)
                putExtra(ConversationActivity.EXTRA_CONVERSATION_CONTACTS, conversation.contacts?.toMutableList() as ArrayList)
            }
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        val divider = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)

        val recyclerView = findViewById<RecyclerView>(R.id.inbox_list)
        recyclerView.adapter = inboxAdapter
        recyclerView.addItemDecoration(divider)

        CoroutineScope(Dispatchers.IO).launch {
            viewModel.getConversations().collectLatest { conversations ->
                inboxAdapter.submitData(conversations)
            }
        }
    }
}