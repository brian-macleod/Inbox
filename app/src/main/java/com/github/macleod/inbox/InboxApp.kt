package com.github.macleod.inbox

import android.app.Application
import android.content.Context

class InboxApp: Application()
{
    /**
     * Companion
     *
     * @constructor Create empty Companion
     */
    companion object
    {
        lateinit var context: Context
    }

    /**
     * On create
     *
     */
    override fun onCreate()
    {
        super.onCreate()
        context = applicationContext
    }
}