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

package de.julianostarek.music.fragments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CoordinatorLayout
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.app.Fragment
import android.support.v4.util.Pair
import android.support.v4.view.ViewCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import co.metalab.asyncawait.async
import de.julianostarek.music.R
import de.julianostarek.music.activities.FavoritesDetailActivity
import de.julianostarek.music.anko.ListSection
import de.julianostarek.music.anko.viewholders.*
import de.julianostarek.music.extensions.getFavorites
import de.julianostarek.music.helper.RequestCodes
import de.julianostarek.music.recyclerview.itemdecorations.ItemDecorations
import mobile.substance.sdk.music.core.dataLinkers.MusicData
import mobile.substance.sdk.music.core.objects.*
import mobile.substance.sdk.music.loading.Library
import mobile.substance.sdk.music.loading.LibraryListener
import mobile.substance.sdk.music.loading.MusicType
import mobile.substance.sdk.music.playback.PlaybackRemote
import org.jetbrains.anko.*
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.support.v4.UI
import org.jetbrains.anko.support.v4.dip

class GenericMusicListFragment : Fragment, LibraryListener {
    lateinit var type: MusicType
    lateinit var recyclerView: RecyclerView
        private set
    private var noData: Boolean = false
    private val orientation: Int by lazy {
        res.configuration.orientation
    }
    private val res: Resources by lazy {
        resources
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) = recyclerView.adapter.notifyItemChanged(0)
    }

    constructor() : super()

    constructor(type: MusicType) : super() {
        this.type = type
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putSerializable("type", type)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return UI {
            recyclerView {
                recyclerView = this
                setHasFixedSize(true)
                clipToPadding = false
                bottomPadding = dip(56)
                val params = CoordinatorLayout.LayoutParams(matchParent, matchParent)
                params.behavior = AppBarLayout.ScrollingViewBehavior()
                layoutParams = params
            }
        }.view
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState != null) type = savedInstanceState.getSerializable("type") as MusicType

        when (type) {
            MusicType.SONGS -> recyclerView.layoutManager = LinearLayoutManager(activity)
            MusicType.ALBUMS, MusicType.ARTISTS -> {
                val spanCount = if (orientation == Configuration.ORIENTATION_PORTRAIT) 3 else 4
                val layoutManager = GridLayoutManager(activity, spanCount)
                layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        return if (position > 1) 1 else layoutManager.spanCount
                    }
                }
                recyclerView.layoutManager = layoutManager
                recyclerView.addItemDecoration(ItemDecorations.VerticalGridSpacing(activity, spanCount, includeEdge = true, startAtPosition = 2))
            }
            MusicType.PLAYLISTS -> {
                val spanCount = if (orientation == Configuration.ORIENTATION_PORTRAIT) 1 else 2
                recyclerView.layoutManager = if (spanCount == 1) LinearLayoutManager(activity) else GridLayoutManager(activity, spanCount).apply {
                    spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                        override fun getSpanSize(position: Int): Int {
                            return if (MusicData.getPlaylists().isEmpty()) this@apply.spanCount else 1
                        }
                    }
                }
                recyclerView.addItemDecoration(ItemDecorations.VerticalGridSpacing(activity, spanCount, includeEdge = true))
            }
        }

        Library.registerBuildFinishedListener({
            updateContent()
            Library.registerListener(this)
        }, true)

        if (type != MusicType.PLAYLISTS) activity.registerReceiver(receiver, IntentFilter("de.julianostarek.music.favorites.${type.name.toLowerCase()}.CHANGE"))

    }

    override fun onDestroyView() {
        if (type != MusicType.PLAYLISTS) activity.unregisterReceiver(receiver)
        try {
            Library.unregisterListener(this)
        } catch (ignored: Exception) {
        }
        super.onDestroyView()
    }

    inner class Adapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
            if (noData) {
                when (type) {
                    MusicType.SONGS, MusicType.ALBUMS, MusicType.ARTISTS -> {
                        (holder as EmptyStateViewHolder).ui.title?.textResource = R.string.no_music
                        holder.ui.image?.imageResource = R.drawable.empty_state_no_music
                    }
                    MusicType.PLAYLISTS -> (holder as EmptyStateViewHolder).ui.image?.imageResource = R.drawable.empty_state_playlists
                }
            } else if (type != MusicType.PLAYLISTS) {
                if (position == 0) {
                    fun getTransitionPair(index: Int): Pair<View, String> {
                        val view = ((holder as UniversalViewHolder<*>).ui as ListSection.ViewHolder.UI.EmptySupported).recyclerView.getChildAt(index)
                        return Pair.create(view, index.toString())
                    }
                    ListSection.handleBinding(holder as ListSection.ViewHolder, getString(R.string.favorites), getString(R.string.more), View.OnClickListener {
                        val intent = Intent(activity, FavoritesDetailActivity::class.java).setAction(type.name.toLowerCase())
                        val recyclerViewPair = Pair<View, String>(holder.itemView, getString(R.string.transition_name_background))
                        val titlePair = Pair<View, String>((holder as ListSection.ViewHolder).ui.title, getString(R.string.transition_name_title))
                        val transitionPairs = Array((holder.ui as ListSection.ViewHolder.UI.EmptySupported).recyclerView.childCount, ::getTransitionPair)
                        activity.startActivityForResult(intent, RequestCodes.FAVORITES_REQUEST_CODE, ActivityOptionsCompat.makeSceneTransitionAnimation(activity, recyclerViewPair, titlePair, *transitionPairs).toBundle())
                    }, InListRecyclerViewAdapter())
                } else if (position == 1) ((holder as ListSection.HeaderViewHolder).ui as ListSection.HeaderViewHolder.UI).apply {
                    title?.text = getString(R.string.all)
                    if (type == MusicType.SONGS) {
                        button.text = getString(R.string.shuffle)
                        button.setOnClickListener { PlaybackRemote.shuffle() }
                    } else button.visibility = View.GONE
                } else when (type) {
                    MusicType.SONGS -> (holder as SongViewHolders.Normal).item = MusicData.getSongs()[position - 2]
                    MusicType.ALBUMS -> (holder as AlbumViewHolder).item = MusicData.getAlbums()[position - 2]
                    MusicType.ARTISTS -> (holder as ArtistViewHolder).item = MusicData.getArtists()[position - 2]
                }
            } else (holder as PlaylistViewHolders.Normal).item = MusicData.getPlaylists()[position]
        }

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
            val ui = getUI(viewType)
            return getViewHolder(ui, viewType, parent!!)
        }

        private fun getUI(viewType: Int): AnkoComponent<ViewGroup> {
            when (viewType) {
                0 -> when (type) {
                    MusicType.SONGS -> return SongViewHolders.Normal.UI()
                    MusicType.ALBUMS -> return AlbumViewHolder.UI()
                    MusicType.ARTISTS -> return ArtistViewHolder.UI()
                    MusicType.PLAYLISTS -> return PlaylistViewHolders.Normal.UI()
                    else -> return null as AnkoComponent<ViewGroup>
                }
                1 -> return ListSection.ViewHolder.UI.EmptySupported()
                2 -> return ListSection.HeaderViewHolder.UI()
                3 -> return EmptyStateViewHolder.UI(if (type == MusicType.PLAYLISTS) dip(48) else 0)
                else -> return null as AnkoComponent<ViewGroup>
            }
        }

        private fun getViewHolder(ui: AnkoComponent<ViewGroup>, viewType: Int, parent: ViewGroup): RecyclerView.ViewHolder {
            val itemView = ui.createView(AnkoContext.create(activity, parent))
            when (viewType) {
                0 -> when (type) {
                    MusicType.SONGS -> return SongViewHolders.Normal(itemView, ui as SongViewHolders.Normal.UI, activity as AppCompatActivity)
                    MusicType.ALBUMS -> return AlbumViewHolder(itemView, ui as AlbumViewHolder.UI, activity as AppCompatActivity)
                    MusicType.ARTISTS -> return ArtistViewHolder(itemView, ui as ArtistViewHolder.UI, activity as AppCompatActivity)
                    MusicType.PLAYLISTS -> return PlaylistViewHolders.Normal(itemView, ui as PlaylistViewHolders.Normal.UI, activity as AppCompatActivity)
                    else -> return null as UniversalViewHolder<*>
                }
                1 -> return ListSection.ViewHolder(itemView, ui as ListSection.ViewHolder.UI.EmptySupported, getInListRecyclerViewHeight(), getInListRecyclerViewLayoutManager(), getInListRecyclerViewItemDecoration(), activity as AppCompatActivity)
                2 -> return ListSection.HeaderViewHolder(itemView, ui as ListSection.HeaderViewHolder.UI, activity as AppCompatActivity)
                3 -> return EmptyStateViewHolder(itemView, ui as EmptyStateViewHolder.UI, activity as AppCompatActivity)
                else -> return null as UniversalViewHolder<*>
            }
        }

        override fun getItemCount(): Int {
            when (type) {
                MusicType.SONGS -> {
                    val size = MusicData.getSongs().size
                    if (size == 0) {
                        noData = true
                        return 1
                    }
                    return size + 2
                }
                MusicType.ALBUMS -> {
                    val size = MusicData.getAlbums().size
                    if (size == 0) {
                        noData = true
                        return 1
                    }
                    return size + 2
                }
                MusicType.ARTISTS -> {
                    val size = MusicData.getArtists().size
                    if (size == 0) {
                        noData = true
                        return 1
                    }
                    return size + 2
                }
                MusicType.PLAYLISTS -> {
                    val size = MusicData.getPlaylists().size
                    if (size == 0) {
                        noData = true
                        return 1
                    }
                    return size
                }
                else -> return 0
            }
        }

        override fun getItemViewType(position: Int): Int {
            if (noData) return 3
            if (position < 2 && type != MusicType.PLAYLISTS) return position + 1
            return 0
        }

    }

    inner class InListRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        private var items: List<MediaObject> = emptyList()

        init {
            when (type) {
                MusicType.SONGS -> async {
                    items = getFavorites(activity, type, if (orientation == Configuration.ORIENTATION_PORTRAIT) 4 else 6, false)
                    notifyDataSetChanged()
                }
                MusicType.ALBUMS, MusicType.ARTISTS -> async {
                    items = getFavorites(activity, type, if (orientation == Configuration.ORIENTATION_PORTRAIT) 3 else 4, false)
                    notifyDataSetChanged()
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
            val ui = getUI()
            val itemView = ui.createView(AnkoContext.create(activity, parent!!))
            when (type) {
                MusicType.SONGS -> return SongViewHolders.Favorite(itemView, ui as SongViewHolders.Favorite.UI, activity as AppCompatActivity)
                MusicType.ALBUMS -> return ImageViewHolders.Horizontal<Album>(itemView, ui as ImageViewHolders.Horizontal.UI, activity as AppCompatActivity)
                MusicType.ARTISTS -> return ImageViewHolders.Horizontal<Artist>(itemView, ui as ImageViewHolders.Horizontal.UI, activity as AppCompatActivity)
                else -> return null as RecyclerView.ViewHolder
            }
        }

        private fun getUI(): AnkoComponent<ViewGroup> {
            when (type) {
                MusicType.SONGS -> return SongViewHolders.Favorite.UI()
                MusicType.ALBUMS, MusicType.ARTISTS -> return ImageViewHolders.Horizontal.UI()
                else -> return null as AnkoComponent<ViewGroup>
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
            when (type) {
                MusicType.SONGS -> (holder as SongViewHolders.Favorite).item = items[position] as Song
                MusicType.ALBUMS -> (holder as ImageViewHolders.Horizontal<Album>).item = items[position] as Album
                MusicType.ARTISTS -> (holder as ImageViewHolders.Horizontal<Artist>).item = items[position] as Artist
            }
        }

        override fun getItemCount(): Int {
            return items.size
        }

    }

    private fun getInListRecyclerViewHeight(): Int {
        when (type) {
            MusicType.SONGS -> return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 148F, res.displayMetrics).toInt()
            MusicType.ALBUMS, MusicType.ARTISTS -> if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                return dip(8) + (res.displayMetrics.widthPixels - dip(16)) / 3
            } else return dip(8) + (res.displayMetrics.widthPixels - dip(20)) / 4
            else -> return 0
        }
    }

    private fun getInListRecyclerViewLayoutManager(): RecyclerView.LayoutManager {
        when (type) {
            MusicType.SONGS -> return GridLayoutManager(activity, if (orientation == Configuration.ORIENTATION_PORTRAIT) 2 else 3)
            MusicType.ALBUMS, MusicType.ARTISTS -> return GridLayoutManager(activity, if (orientation == Configuration.ORIENTATION_PORTRAIT) 3 else 4)
            else -> return null as RecyclerView.LayoutManager
        }
    }

    private fun getInListRecyclerViewItemDecoration(): RecyclerView.ItemDecoration {
        when (type) {
            MusicType.SONGS -> return ItemDecorations.VerticalGridSpacing(activity, if (orientation == Configuration.ORIENTATION_PORTRAIT) 2 else 3, includeEdge = true)
            MusicType.ALBUMS, MusicType.ARTISTS -> return ItemDecorations.VerticalGridSpacing(activity, if (orientation == Configuration.ORIENTATION_PORTRAIT) 3 else 4, includeEdge = true)
            else -> return null as RecyclerView.ItemDecoration
        }
    }

    fun updateContent() {
        noData = false
        recyclerView.adapter = Adapter()
    }

    override fun onAlbumLoaded(item: Album, pos: Int) {

    }

    override fun onAlbumsCompleted(result: List<Album>) {
        if (type == MusicType.ALBUMS) updateContent()
    }

    override fun onArtistLoaded(item: Artist, pos: Int) {

    }

    override fun onArtistsCompleted(result: List<Artist>) {
        if (type == MusicType.ARTISTS) updateContent()
    }

    override fun onGenreLoaded(item: Genre, pos: Int) {

    }

    override fun onGenresCompleted(result: List<Genre>) {

    }

    override fun onPlaylistLoaded(item: Playlist, pos: Int) {

    }

    override fun onPlaylistsCompleted(result: List<Playlist>) {
        if (type == MusicType.PLAYLISTS) updateContent()
    }

    override fun onSongLoaded(item: Song, pos: Int) {

    }

    override fun onSongsCompleted(result: List<Song>) {
        if (type == MusicType.SONGS) updateContent()
    }

    fun hasScrolled(): Boolean {
        try {
            val layoutManager = recyclerView.layoutManager
            when (layoutManager) {
                is LinearLayoutManager -> return layoutManager.findFirstCompletelyVisibleItemPosition() > 0
                is GridLayoutManager -> return layoutManager.findFirstCompletelyVisibleItemPosition() > 0
            }
        } catch (e: UninitializedPropertyAccessException) {
            return false
        }
        return false
    }
}