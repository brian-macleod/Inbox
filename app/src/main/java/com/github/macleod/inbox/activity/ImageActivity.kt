package com.github.macleod.inbox.activity

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.github.macleod.inbox.R
import com.github.macleod.inbox.adapter.ImageAdapter
import com.github.macleod.inbox.data.repository.MessageRepository
import com.github.macleod.inbox.viewmodel.ImageViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ImageActivity : AppCompatActivity()
{
    companion object
    {
        const val EXTRA_CONVERSATION_ID = "com.github.macleod.inbox.activity.ImageActivity.EXTRA_CONVERSATION_ID"
        const val EXTRA_PART_ID = "com.github.macleod.inbox.activity.ImageActivity.EXTRA_PART_ID"
    }

    /**
     * On create
     *
     * @param savedInstanceState
     */
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image)

        val conversationID = intent.getLongExtra(EXTRA_CONVERSATION_ID, -1)
        val partID = intent.getLongExtra(EXTRA_PART_ID, -1)

        setSupportActionBar(findViewById(R.id.image_toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val messageRepository = MessageRepository()
        val viewModelFactory = ImageViewModel.Factory(messageRepository)
        val viewModel = ViewModelProvider(this, viewModelFactory).get(ImageViewModel::class.java)

        val imageAdapter = ImageAdapter()

        val viewPager = findViewById<ViewPager2>(R.id.image_view_pager)
        viewPager.adapter = imageAdapter
        viewPager.layoutDirection = ViewPager.LAYOUT_DIRECTION_RTL

        CoroutineScope(Dispatchers.IO).launch {
            val imageIDs = viewModel.getImageIDs(conversationID)

            var startingIndex = 0
            for (i in imageIDs.indices.reversed())
            {
                if (imageIDs[i] == partID)
                {
                    startingIndex = i
                    break
                }
            }

            viewModel.getImages(imageIDs, startingIndex).collectLatest { images ->
                imageAdapter.submitData(images)
            }
        }
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
}