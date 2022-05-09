package com.github.macleod.inbox.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView

abstract class ViewHolder<T>(view: View): RecyclerView.ViewHolder(view)
{
    /**
     * Bind
     *
     * @param item
     */
    abstract fun bind(item: T)
}