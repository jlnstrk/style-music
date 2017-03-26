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

package de.julianostarek.music.helper

import android.content.Intent
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.app.ActivityOptionsCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.PopupMenu
import android.view.Gravity
import android.view.View
import co.metalab.asyncawait.async
import com.afollestad.materialdialogs.MaterialDialog
import de.julianostarek.music.R
import de.julianostarek.music.activities.AlbumActivity
import de.julianostarek.music.activities.ArtistActivity
import de.julianostarek.music.extensions.isFavorite
import de.julianostarek.music.extensions.setFavorite
import mobile.substance.sdk.music.core.dataLinkers.MusicData
import mobile.substance.sdk.music.core.objects.Song
import mobile.substance.sdk.music.playback.PlaybackRemote
import mobile.substance.sdk.utils.MusicCoreUtil
import mobile.substance.sdk.utils.MusicTagsUtil
import java.io.File
import java.util.*

object PopupHelper {

    @JvmStatic fun showPopup(view: View, song: Song, activity: AppCompatActivity, allowDeletion: Boolean = false) = async {
        val popup = PopupMenu(activity, view, Gravity.END or Gravity.CENTER_VERTICAL)
        popup.inflate(R.menu.menu_popup)
        if (allowDeletion) {
            popup.menu.findItem(R.id.popup_delete).isVisible = true
        }
        if (await { song.isFavorite(activity) }) {
            val item = popup.menu.findItem(R.id.popup_add_to_favorites)
            item.title = activity.getString(R.string.remove_from_favorites)
        }
        popup.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.popup_add_to_favorites -> {
                    async {
                        await { song.setFavorite(activity, !song.isFavorite(activity)) }
                    }
                    true
                }
                R.id.popup_add_to_queue -> {
                    PlaybackRemote.addToQueue(song, true)
                    true
                }
                R.id.popup_play_next -> {
                    PlaybackRemote.addToQueueAsNext(song, true)
                    true
                }
                R.id.popup_add_to_playlist -> {
                    val names = ArrayList<String>()
                    MusicData.getPlaylists().forEach { names.add(it.playlistName.orEmpty()) }
                    MaterialDialog.Builder(activity)
                            .title(R.string.add_to_playlist)
                            .items(names)
                            .itemsCallback { _, _, i, _ ->
                                MusicTagsUtil.addToPlaylist(activity, listOf(song.id), MusicData.getPlaylists()[i].id)
                            }
                            .show()
                    true
                }
                R.id.popup_album -> {
                    ActivityCompat.startActivity(activity, Intent(activity, AlbumActivity::class.java).putExtra("album_id", song.songAlbumId), ActivityOptionsCompat.makeSceneTransitionAnimation(activity).toBundle())
                    true
                }
                R.id.popup_artist -> {
                    ActivityCompat.startActivity(activity, Intent(activity, ArtistActivity::class.java).putExtra("artist_id", song.songArtistId), ActivityOptionsCompat.makeSceneTransitionAnimation(activity).toBundle())
                    true
                }
                R.id.popup_delete -> {
                    val actualPath = File(MusicCoreUtil.getFilePath(activity, song.uri))
                    activity.contentResolver.delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, "${MediaStore.Audio.Media._ID} = ?", arrayOf(song.id.toString()))
                    actualPath.deleteRecursively()
                }
                else -> false
            }
        }
        popup.show()
    }

}