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

package de.julianostarek.music.activities

import android.content.Intent
import android.graphics.BitmapFactory
import android.support.design.widget.Snackbar
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Html
import android.util.TypedValue
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.afollestad.materialdialogs.MaterialDialog
import de.julianostarek.music.R
import de.julianostarek.music.adapters.MusicAdapter
import de.julianostarek.music.anko.DetailHeader
import de.julianostarek.music.anko.NestedRecyclerView
import de.julianostarek.music.anko.ListHeader
import de.julianostarek.music.anko.viewholders.ImageViewHolders
import de.julianostarek.music.anko.viewholders.SongViewHolders
import de.julianostarek.music.anko.viewholders.UniversalViewHolder
import de.julianostarek.music.databases.MetadataHelper
import de.julianostarek.music.extensions.addPlayed
import de.julianostarek.music.extensions.setImageResourceTinted
import de.julianostarek.music.recyclerview.itemdecorations.ItemDecorations
import mobile.substance.sdk.colors.DynamicColors
import mobile.substance.sdk.music.core.dataLinkers.MusicData
import mobile.substance.sdk.music.core.objects.Album
import mobile.substance.sdk.music.core.objects.Artist
import mobile.substance.sdk.music.playback.PlaybackRemote
import org.jetbrains.anko.AnkoComponent
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.dip
import org.jetbrains.anko.leftPadding
import java.io.File
import java.io.IOException

class ArtistActivity : ItemDetailActivity<Artist, RecyclerView.ViewHolder>() {

    override val menuResId: Int = R.menu.menu_artist

    override val defaultArtworkResId: Int = R.drawable.placeholder_artist

    override fun findMediaObject(intent: Intent): Artist = MusicData.findArtistById(intent.getLongExtra("artist_id", 0))!!

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.item_biography) {
            if (MetadataHelper.contains(this, mediaObject.id, Artist::class.java.simpleName.toLowerCase() + "_biography")) {
                var biography: String? = null
                try {
                    biography = File(MetadataHelper.getPath(this, mediaObject.id, Artist::class.java.simpleName.toLowerCase() + "_biography")).readText()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                if (biography != null) {
                    MaterialDialog.Builder(this)
                            .title(R.string.bio)
                            .content(Html.fromHtml(biography))
                            .show()
                    return true
                }
            }
            Snackbar.make(findViewById(android.R.id.content), getString(R.string.no_bio), Snackbar.LENGTH_SHORT).show()
            return false
        }
        return super.onMenuItemClick(item)
    }

    override fun onClick(p0: View?) {
        when (p0) {
            floatingActionButton -> {
                mediaObject.addPlayed(this)
                PlaybackRemote.play(MusicData.findSongsForArtist(mediaObject), 0)
            }
            else -> onBackPressed()
        }
    }

    override fun setArtwork(imageView: ImageView): DynamicColors? {
        if (mediaObject.artistImagePath != null) {
            val bitmap = BitmapFactory.decodeFile(mediaObject.artistImagePath)
            if (bitmap != null) {
                image.setImageBitmap(bitmap)
                return DynamicColors.from(bitmap)
            } else return null
        } else return null
    }

    override fun getAdapter(): RecyclerView.Adapter<RecyclerView.ViewHolder> = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        val songs = MusicData.findSongsForArtist(mediaObject)
        val albums = MusicData.findAlbumsForArtist(mediaObject)

        override fun getItemCount(): Int {
            return songs.size + if (albums.isNotEmpty()) 4 else 2
        }

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
            when (viewType) {
                1 -> {
                    val ui = DetailHeader.UI(advancedTransition)
                    val viewHolder = DetailHeader(ui.createView(AnkoContext.create(this@ArtistActivity, parent!!)), ui, this@ArtistActivity)
                    if (mediaObject.artistImagePath == null) viewHolder.itemView.setBackgroundColor(0xFFFAFAFA.toInt())
                    return viewHolder
                }
                2 -> {
                    val ui = ListHeader.UI()
                    val viewHolder = ListHeader(ui.createView(AnkoContext.Companion.create(this@ArtistActivity, parent!!)), ui, this@ArtistActivity)
                    viewHolder.ui.title?.setPadding(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 72F, resources.displayMetrics).toInt(), 0, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16F, resources.displayMetrics).toInt(), 0)
                    return viewHolder
                }
                3 -> {
                    val ui = NestedRecyclerView.UI()
                    return NestedRecyclerView(ui.createView(AnkoContext.create(this@ArtistActivity, parent!!)), ui).apply {
                        ui.recyclerView.layoutManager = LinearLayoutManager(this@ArtistActivity, LinearLayoutManager.HORIZONTAL, false)
                        ui.recyclerView.addItemDecoration(ItemDecorations.HorizontalLinearPaddingLeftSpacing(this@ArtistActivity))
                    }
                }
                else -> {
                    val ui = SongViewHolders.Normal.UI()
                    val view = ui.createView(AnkoContext.create(this@ArtistActivity, parent!!))
                    view.leftPadding = dip(56)
                    val viewHolder = SongViewHolders.Normal(view, ui, this@ArtistActivity)
                    viewHolder.itemView.setOnClickListener {
                        mediaObject.addPlayed(this@ArtistActivity)
                        PlaybackRemote.play(songs, songs.indexOf(viewHolder.item))
                    }
                    return viewHolder
                }
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
            if (position == 0) {
                holder as UniversalViewHolder<*>
                holder.ui.title?.text = mediaObject.artistName
                // The "quantity" parameter is ignored (idk why) so we just pass the number to the vararg as well
                holder.ui.subtitle?.text = resources.getQuantityString(R.plurals.numberOfAlbums, albums.size, albums.size) + " • " + resources.getQuantityString(R.plurals.numberOfSongs, songs.size, songs.size)
                (holder.ui as DetailHeader.UI).icon.setImageResourceTinted(R.drawable.ic_artist_info_black_24dp, colors.primaryDarkColor)
            } else if (position == 1 || (position == 3 && albums.isNotEmpty())) {
                holder as UniversalViewHolder<*>
                holder.ui.title?.text = if (position == 1 && albums.isNotEmpty()) getString(R.string.albums) else getString(R.string.songs)
            } else if (position == 2 && albums.isNotEmpty()) {
                holder as NestedRecyclerView
                holder.ui.recyclerView.adapter = object : MusicAdapter<Album>() {

                    init {
                        setData(albums)
                    }

                    override fun getViewHolder(view: View, ui: AnkoComponent<ViewGroup>, itemType: Int): UniversalViewHolder<Album> {
                        return ImageViewHolders.Horizontal<Album>(view, ui as ImageViewHolders.Horizontal.UI, this@ArtistActivity)
                    }

                    override fun getUI(itemType: Int): AnkoComponent<ViewGroup> {
                        return ImageViewHolders.Horizontal.UI()
                    }

                    override fun bindItem(item: Album, holder: UniversalViewHolder<Album>, position: Int) {
                        getData()[position].requestArt(holder.ui.image!!)
                        holder.item = item
                    }
                }
            } else (holder as SongViewHolders.Normal).item = songs[position - if (albums.isNotEmpty()) 4 else 2]
        }

        override fun getItemViewType(position: Int): Int {
            if (albums.isNotEmpty()) {
                when (position) {
                    0 -> return 1
                    1 -> return 2
                    2 -> return 3
                    3 -> return 2
                    else -> return 0
                }
            } else when (position) {
                0 -> return 1
                1 -> return 2
                else -> return 0
            }
        }

    }

}