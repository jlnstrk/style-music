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

import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ImageView
import co.metalab.asyncawait.async
import com.bumptech.glide.Glide
import de.julianostarek.mfb.FolderBrowserConfig
import de.julianostarek.mfb.FolderBrowserFragment
import de.julianostarek.music.R
import de.julianostarek.music.extensions.setupWithNavigationDrawer
import de.julianostarek.music.helper.PopupHelper
import mobile.substance.sdk.music.core.dataLinkers.MusicData
import mobile.substance.sdk.music.core.objects.Song
import mobile.substance.sdk.music.playback.PlaybackRemote
import org.jetbrains.anko.bottomPadding
import org.jetbrains.anko.dip
import java.io.File
import kotlin.concurrent.thread

class StyleFolderBrowserFragment : FolderBrowserFragment() {

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar.setupWithNavigationDrawer(activity)
        recyclerView.apply {
            clipToPadding = false
            bottomPadding = dip(56)
        }
    }

    override fun getConfig(): FolderBrowserConfig {
        return FolderBrowserConfig.Builder()
                .setToolbarTitle(R.string.folders)
                .setRootDirectory("/")
                .setInitialDirectory(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC))
                .setSupportedFileExtensions("mp3", "ogg", "m4a", "wav", "flac", "aac", "3gp", "mp4", "ts", "mkv", "wma")
                .build()
    }

    override fun useDefaultThumbnail(imageView: ImageView, file: File): Boolean {
        Glide.clear(imageView)
        async {
            Glide.with(activity).load(await {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(file.path)
                retriever.embeddedPicture
            }).crossFade().error(R.drawable.mfb_ic_file_black_24dp).into(imageView)
        }
        return false
    }

    override fun showPopupMenu(file: File, anchor: View): Boolean {
        PopupHelper.showPopup(anchor, file.toSong()!!, activity as AppCompatActivity, true)
        return true
    }

    fun File.toSong(): Song? {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(path)

        return MusicData.getSongs().firstOrNull { it -> (it.songTitle?.equals(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)) ?: false) && (it.songArtistName?.equals(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)) ?: false) && (it.songAlbumName?.equals(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)) ?: false) }
    }

    override fun onFileSelected(file: File) {
        thread {
            val song = file.toSong()
            if (song != null) activity.runOnUiThread { PlaybackRemote.play(song) }
        }
    }
}