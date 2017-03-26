/*
 * Copyright 2017 Julian Ostarek
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.julianostarek.music.adapters

import android.support.v4.view.MotionEventCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.MotionEvent
import android.view.ViewGroup
import de.julianostarek.music.anko.viewholders.QueueItemViewHolder
import mobile.substance.sdk.music.core.objects.Song
import mobile.substance.sdk.music.playback.PlaybackRemote
import org.jetbrains.anko.AnkoContext

class QueueAdapter(private val startDragListener: (QueueItemViewHolder) -> Unit, private val activity: AppCompatActivity) : RecyclerView.Adapter<QueueItemViewHolder>() {

    private val queue: List<Song>
        get() = PlaybackRemote.getQueue(true).orEmpty()

    fun onQueueChanged() {
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): QueueItemViewHolder {
        val ui = QueueItemViewHolder.UI()
        return QueueItemViewHolder(ui.createView(AnkoContext.create(parent!!.context, parent)), ui, activity)
    }

    override fun getItemCount(): Int {
        return queue.size
    }

    override fun onBindViewHolder(holder: QueueItemViewHolder, position: Int) {
        val item = queue[position]
        holder.ui.image?.setOnTouchListener { _, motionEvent ->
            if (MotionEventCompat.getActionMasked(motionEvent) == MotionEvent.ACTION_DOWN) startDragListener.invoke(holder)
            false
        }
        (holder.ui as QueueItemViewHolder.UI).index.text = (position + 2).toString()
        holder.item = item
    }

}