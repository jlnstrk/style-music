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

import android.content.Context
import android.support.v4.view.ViewCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.view.ViewGroup
import de.julianostarek.music.anko.viewholders.MediaItemViewHolders
import de.julianostarek.music.anko.viewholders.PlaylistViewHolders
import de.julianostarek.music.anko.viewholders.UniversalViewHolder
import mobile.substance.sdk.music.core.objects.*
import org.jetbrains.anko.AnkoContext

abstract class HorizontalMediaItemsAdapter(val context: Context, private val isThirdbig: Boolean = false) : RecyclerView.Adapter<UniversalViewHolder<*>>() {
    var items: List<MediaObject>? = null

    override fun onBindViewHolder(holder: UniversalViewHolder<*>?, position: Int) {
        val item = items!![position]
        ViewCompat.setTransitionName(holder?.itemView, position.toString())

        if (position == 0) {
            val params = StaggeredGridLayoutManager.LayoutParams(StaggeredGridLayoutManager.LayoutParams.WRAP_CONTENT, StaggeredGridLayoutManager.LayoutParams.MATCH_PARENT)
            params.isFullSpan = true
            holder?.itemView?.layoutParams = params
        }

        when (item) {
            is Song -> (holder as UniversalViewHolder<Song>).item = item
            is Album -> (holder as UniversalViewHolder<Album>).item = item
            is Artist -> (holder as UniversalViewHolder<Artist>).item = item
            is Playlist -> (holder as UniversalViewHolder<Playlist>).item = item
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UniversalViewHolder<*> {
        when (viewType) {
            0 -> {
                val ui = MediaItemViewHolders.Big.UI()
                return MediaItemViewHolders.Big<MediaObject>(ui.createView(AnkoContext.create(context, parent)), ui, context as AppCompatActivity)
            }
            1 -> {
                val ui = MediaItemViewHolders.Small.UI()
                return MediaItemViewHolders.Small(ui.createView(AnkoContext.create(context, parent)), ui, context as AppCompatActivity)
            }
            2 -> {
                val ui = PlaylistViewHolders.Small.UI()
                return PlaylistViewHolders.Small(ui.createView(AnkoContext.create(context, parent)), ui, context as AppCompatActivity)
            }
            else -> return null as UniversalViewHolder<*>
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (position == if (isThirdbig) 2 else 0) return 0 else {
            when (items!![position]) {
                is Song,
                is Album,
                is Artist -> return 1
                is Playlist -> return 2
                else -> return 0
            }
        }
    }

    override fun getItemCount(): Int {
        return items?.size ?: 0
    }

}