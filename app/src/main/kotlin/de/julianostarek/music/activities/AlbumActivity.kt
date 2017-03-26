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
import android.support.v4.app.ActivityOptionsCompat
import android.support.v7.widget.RecyclerView
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import de.julianostarek.music.R
import de.julianostarek.music.anko.DetailHeader
import de.julianostarek.music.anko.viewholders.SongViewHolders
import de.julianostarek.music.anko.viewholders.UniversalViewHolder
import de.julianostarek.music.extensions.addPlayed
import de.julianostarek.music.extensions.setImageResourceTinted
import mobile.substance.sdk.colors.DynamicColors
import mobile.substance.sdk.music.core.dataLinkers.MusicData
import mobile.substance.sdk.music.core.objects.Album
import mobile.substance.sdk.music.core.objects.Song
import mobile.substance.sdk.music.playback.PlaybackRemote
import org.jetbrains.anko.AnkoContext


class AlbumActivity : ItemDetailActivity<Album, UniversalViewHolder<*>>() {

    override val menuResId: Int = R.menu.menu_album

    override val defaultArtworkResId: Int = R.drawable.placeholder_album

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.item_show_artist) {
            startActivity(Intent(this, ArtistActivity::class.java).putExtra("artist_id", mediaObject.id), ActivityOptionsCompat.makeSceneTransitionAnimation(this).toBundle())
        }
        return super.onMenuItemClick(item)
    }

    override fun findMediaObject(intent: Intent): Album = MusicData.findAlbumById(intent.getLongExtra("album_id", 0L))!!

    override fun onClick(p0: View?) {
        when (p0) {
            floatingActionButton -> {
                mediaObject.addPlayed(this)
                PlaybackRemote.play(MusicData.findSongsForAlbum(mediaObject), 0)
            }
            else -> onBackPressed()
        }
    }

    override fun setArtwork(imageView: ImageView): DynamicColors? {
        if (mediaObject.albumArtworkPath != null) {
            val bitmap = BitmapFactory.decodeFile(mediaObject.albumArtworkPath)
            if (bitmap != null) {
                image.setImageBitmap(bitmap)
                return DynamicColors.from(bitmap)
            } else return null
        } else return null
    }

    override fun getAdapter(): RecyclerView.Adapter<UniversalViewHolder<*>> = object : RecyclerView.Adapter<UniversalViewHolder<*>>() {
        val items: List<Song> by lazy {
            MusicData.findSongsForAlbum(mediaObject)
        }

        override fun getItemCount(): Int {
            return items.size + 1
        }

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): UniversalViewHolder<*> {
            when (viewType) {
                1 -> {
                    val ui = DetailHeader.UI(advancedTransition)
                    val viewHolder = DetailHeader(ui.createView(AnkoContext.create(this@AlbumActivity, parent!!)), ui, this@AlbumActivity)
                    if (mediaObject.albumArtworkPath == null) viewHolder.itemView.setBackgroundColor(0xFFFAFAFA.toInt())
                    return viewHolder
                }
                else -> {
                    val ui = SongViewHolders.Indexed.UI()
                    val viewHolder = SongViewHolders.Indexed(ui.createView(AnkoContext.create(this@AlbumActivity, parent!!)), ui, this@AlbumActivity)
                    viewHolder.itemView.setOnClickListener {
                        mediaObject.addPlayed(this@AlbumActivity)
                        PlaybackRemote.play(items, items.indexOf(viewHolder.item))
                    }
                    return viewHolder
                }
            }
        }

        override fun onBindViewHolder(holder: UniversalViewHolder<*>?, position: Int) {
            if (position == 0) {
                holder!!.ui.title?.text = mediaObject.albumName
                holder.ui.subtitle?.text = mediaObject.albumArtistName
                (holder.ui as DetailHeader.UI).icon.setImageResourceTinted(R.drawable.ic_shuffle_white_24dp, colors.primaryDarkColor)
            } else (holder as SongViewHolders.Indexed).item = items[position - 1]
        }

        override fun getItemViewType(position: Int): Int {
            when (position) {
                0 -> return 1
                else -> return 0
            }
        }

    }

}

