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

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.support.v7.widget.RecyclerView
import android.text.InputType
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import de.julianostarek.music.R
import de.julianostarek.music.anko.DetailHeader
import de.julianostarek.music.anko.viewholders.SongViewHolders
import de.julianostarek.music.anko.viewholders.UniversalViewHolder
import de.julianostarek.music.databases.MetadataHelper
import de.julianostarek.music.extensions.addPlayed
import de.julianostarek.music.extensions.setImageResourceTinted
import de.julianostarek.music.helper.RequestCodes
import mobile.substance.sdk.colors.DynamicColors
import mobile.substance.sdk.music.core.dataLinkers.MusicData
import mobile.substance.sdk.music.core.objects.Playlist
import mobile.substance.sdk.music.core.objects.Song
import mobile.substance.sdk.music.playback.PlaybackRemote
import mobile.substance.sdk.utils.MusicCoreUtil
import mobile.substance.sdk.utils.MusicTagsUtil
import org.jetbrains.anko.AnkoContext

class PlaylistActivity : ItemDetailActivity<Playlist, UniversalViewHolder<*>>() {
    var hasImage = false

    override val menuResId: Int = R.menu.menu_playlist

    override val defaultArtworkResId: Int = R.drawable.placeholder_playlist

    override fun findMediaObject(intent: Intent): Playlist = MusicData.findPlaylistById(intent.getLongExtra("playlist_id", 0))!!

    override fun onClick(p0: View?) {
        when (p0) {
            floatingActionButton -> {
                val songs = MusicData.findSongsForPlaylist(mediaObject)
                if (songs.isNotEmpty()) {
                    mediaObject.addPlayed(this)
                    PlaybackRemote.play(songs, 0)
                }
            }
            else -> onBackPressed()
        }
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.item_rename -> {
                MaterialDialog.Builder(this)
                        .title(R.string.rename_playlist)
                        .inputType(InputType.TYPE_CLASS_TEXT)
                        .input(getString(R.string.rename_playlist_desc), mediaObject.playlistName, false) { _, input ->
                            mediaObject.playlistName = input.toString()
                            MusicTagsUtil.renamePlaylist(this, mediaObject.id, input.toString())
                            try {
                                (recyclerView.getChildAt(0).findViewById(R.id.item_title) as TextView).text = input
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                        .show()
            }
            R.id.item_change_image -> {
                startActivityForResult(Intent.createChooser(Intent(Intent.ACTION_GET_CONTENT).setType("image/*").putExtra(Intent.EXTRA_LOCAL_ONLY, true), getString(R.string.add_image)), RequestCodes.GET_CONTENT_REQUEST_CODE)
            }
        }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RequestCodes.GET_CONTENT_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val path = MusicCoreUtil.getFilePath(this, data!!.data).orEmpty()
            MetadataHelper.insert(this, mediaObject.id, path, Playlist::class.java.simpleName.toLowerCase())
            hasImage = true
            val bitmap = BitmapFactory.decodeFile(path)
            image.setImageBitmap(bitmap)
            DynamicColors.from(bitmap).generate(true, this)
        }
    }

    override fun setArtwork(imageView: ImageView): DynamicColors? {
        if (MetadataHelper.contains(this, mediaObject.id, Playlist::class.java.simpleName.toLowerCase())) {
            val bitmap = BitmapFactory.decodeFile(MetadataHelper.getPath(this, mediaObject.id, Playlist::class.java.simpleName.toLowerCase()))
            if (bitmap == null) {
                MetadataHelper.delete(this, mediaObject.id, Playlist::class.java.simpleName.toLowerCase())
                return null
            } else {
                image.setImageBitmap(bitmap)
                hasImage = true
                return DynamicColors.from(bitmap)
            }
        } else return null
    }

    override fun getAdapter(): RecyclerView.Adapter<UniversalViewHolder<*>> = object : RecyclerView.Adapter<UniversalViewHolder<*>>() {
        val items: List<Song> by lazy {
            MusicData.findSongsForPlaylist(mediaObject)
        }

        override fun getItemCount(): Int {
            return items.size + 1
        }

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): UniversalViewHolder<*> {
            when (viewType) {
                1 -> {
                    val ui = DetailHeader.UI(advancedTransition)
                    val viewHolder = DetailHeader(ui.createView(AnkoContext.create(this@PlaylistActivity, parent!!)), ui, this@PlaylistActivity)
                    if (!MetadataHelper.contains(this@PlaylistActivity, mediaObject.id, Playlist::class.java.simpleName.toLowerCase())) viewHolder.itemView.setBackgroundColor(0xFFFAFAFA.toInt())
                    return viewHolder
                }
                else -> {
                    val ui = SongViewHolders.Indexed.UI()
                    return SongViewHolders.Indexed(ui.createView(AnkoContext.create(this@PlaylistActivity, parent!!)), ui, this@PlaylistActivity).apply {
                        itemView.setOnClickListener {
                            mediaObject.addPlayed(this@PlaylistActivity)
                            PlaybackRemote.play(items, items.indexOf(item))
                        }
                    }
                }
            }
        }

        override fun onBindViewHolder(holder: UniversalViewHolder<*>?, position: Int) {
            if (position == 0) {
                holder!!.ui.title?.text = mediaObject.playlistName
                // The "quantity" parameter is ignored (idk why) so we just pass the number to the vararg as well
                holder.ui.subtitle?.text = resources.getQuantityString(R.plurals.numberOfSongs, items.size, items.size)
                (holder.ui as DetailHeader.UI).icon.setImageResourceTinted(R.drawable.ic_queue_music_black_24dp, colors.primaryDarkColor)
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
