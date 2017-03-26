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

import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.TextView
import de.julianostarek.music.R
import de.julianostarek.music.anko.NestedRecyclerView
import de.julianostarek.music.anko.ListHeader
import de.julianostarek.music.anko.ListSection
import de.julianostarek.music.anko.viewholders.EmptyStateViewHolder
import de.julianostarek.music.anko.viewholders.SongViewHolders
import de.julianostarek.music.anko.viewholders.UniversalViewHolder
import de.julianostarek.music.helper.AppColors
import de.julianostarek.music.recyclerview.itemdecorations.ItemDecorations
import mobile.substance.sdk.music.core.dataLinkers.MusicData
import mobile.substance.sdk.music.core.objects.Album
import mobile.substance.sdk.music.core.objects.Artist
import mobile.substance.sdk.music.core.objects.Song
import mobile.substance.sdk.music.playback.PlaybackRemote
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.dip
import org.jetbrains.anko.imageResource
import java.util.*

class SearchAdapter(private val activity: AppCompatActivity) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), TextWatcher {
    private val queriedSongs = ArrayList<Song>()
    private var queriedAlbums: List<Album>? = null
    private var queriedArtists: List<Artist>? = null
    private var query: String = ""
    private var noData = false

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

    }

    override fun afterTextChanged(p0: Editable?) {

    }

    override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
        if (s.toString().isEmpty()) {
            queriedSongs.clear()
            queriedAlbums = null
            queriedArtists = null
            notifyDataSetChanged()
            return
        }
        query = s.toString()
        queriedAlbums = MusicData.search<Album>(query)
        queriedArtists = MusicData.search<Artist>(query)
        val queriedSongs = MusicData.search<Song>(query)

        this.queriedSongs.clear()
        var i = 0
        while (i < 4 && i < queriedSongs.orEmpty().size) {
            this.queriedSongs.add(queriedSongs!![i])
            i++
        }
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        when (getItemViewType(position)) {
            0 -> (holder as UniversalViewHolder<*>).ui.title?.text = activity.getString(R.string.songs)
            4 -> (holder as UniversalViewHolder<*>).ui.title?.text = activity.getString(R.string.albums)
            8 -> (holder as UniversalViewHolder<*>).ui.title?.text = activity.getString(R.string.artists)
            3 -> (holder as EmptyStateViewHolder).ui.image?.imageResource = R.drawable.empty_state_search
            1 -> (holder?.itemView as RecyclerView).adapter = SearchResultsListAdapter(if (positionAfterSongItems() < position && position < positionAfterAlbumItems() && queriedAlbums.orEmpty().isNotEmpty()) queriedAlbums!! else queriedArtists!!)
            2 -> {
                holder as SongViewHolders.Normal
                val item = queriedSongs[position - 1]
                holder.item = item
                val spannable = SpannableString(item.songTitle)
                val startIndex = item.songTitle!!.indexOf(query, ignoreCase = true)
                spannable.setSpan(ForegroundColorSpan(AppColors.ACCENT_COLOR), startIndex, startIndex + query.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
                holder.ui.title?.setText(spannable, TextView.BufferType.SPANNABLE)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        var vh: RecyclerView.ViewHolder? = null
        when (viewType) {
            3 -> {
                val ui = EmptyStateViewHolder.UI(activity.dip(56))
                vh = EmptyStateViewHolder(ui.createView(AnkoContext.create(parent!!.context, parent)), ui, activity)
                return vh
            }
            0 -> {
                val ui = ListSection.HeaderViewHolder.UI()
                vh = ListSection.HeaderViewHolder(ui.createView(AnkoContext.Companion.create(parent!!.context, parent)), ui, activity)
                ui.button.setOnClickListener {
                    Snackbar.make(activity.findViewById(android.R.id.content), "Coming soon!", Snackbar.LENGTH_SHORT).show()
                }
                vh.itemView?.setPadding(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 56F, parent.context?.resources?.displayMetrics).toInt(), 0, 0, 0)
            }
            4, 8 -> {
                val ui = ListHeader.UI()
                vh = ListHeader(ui.createView(AnkoContext.create(parent!!.context, parent)), ui, activity)
                vh.itemView?.setPadding(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 72F, parent.context?.resources?.displayMetrics).toInt(), 0, 0, 0)
            }
            1 -> {
                val ui = NestedRecyclerView.UI()
                vh = NestedRecyclerView(ui.createView(AnkoContext.create(parent!!.context, parent)), ui)
                (vh.itemView as RecyclerView).layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.HORIZONTAL)
                (vh.itemView as RecyclerView).addItemDecoration(ItemDecorations.ThreeItemsStaggeredGrid(vh.itemView.context))
                vh.itemView?.layoutParams?.height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 256F, parent.context.resources.displayMetrics).toInt()
                vh.itemView?.setPadding(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 68F, parent.context.resources.displayMetrics).toInt(), vh.itemView!!.paddingTop, vh.itemView!!.paddingRight, vh.itemView?.paddingBottom!!)
            }
            2 -> {
                val ui = SongViewHolders.Normal.UI()
                vh = SongViewHolders.Normal(ui.createView(AnkoContext.Companion.create(parent!!.context, parent)), ui, activity)
                vh.itemView?.setOnClickListener {
                    PlaybackRemote.play((vh as SongViewHolders.Normal).item!!)
                }
                vh.itemView?.setPadding(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 56F, parent.context?.resources?.displayMetrics).toInt(), 0, 0, 0)
            }
        }
        vh?.itemView?.setPadding(vh.itemView?.paddingLeft ?: 0 + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 56F, parent?.context?.resources?.displayMetrics!!).toInt(), vh.itemView?.paddingTop ?: 0, vh.itemView?.paddingRight ?: 0, vh.itemView?.paddingBottom ?: 0)
        return vh!!
    }

    override fun getItemCount(): Int {
        val it = (if (queriedSongs.size > 0) queriedSongs.size + 1 else 0) + (if (queriedAlbums.orEmpty().isNotEmpty()) 2 else 0) + if (queriedArtists.orEmpty().isNotEmpty()) 2 else 0
        noData = it == 0
        return if (it > 0) it else 1
    }

    private fun positionAfterSongItems(): Int {
        return if (queriedSongs.orEmpty().isEmpty()) 0 else queriedSongs.size + 1
    }

    private fun positionAfterAlbumItems(): Int {
        return positionAfterSongItems() + if (queriedAlbums.orEmpty().isEmpty()) 0 else 2
    }

    override fun getItemViewType(position: Int): Int {
        if (noData) return 3
        if (position < positionAfterSongItems()) {
            return if (position == 0) 0 else 2
        }

        if (position == positionAfterSongItems() && queriedAlbums.orEmpty().isNotEmpty()) return 4
        if (position == positionAfterAlbumItems()) return 8
        return 1
    }
}