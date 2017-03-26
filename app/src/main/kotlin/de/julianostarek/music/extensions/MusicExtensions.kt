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

package de.julianostarek.music.extensions

import android.widget.ImageView
import co.metalab.asyncawait.async
import com.bumptech.glide.Glide
import de.julianostarek.music.R
import de.julianostarek.music.databases.MetadataHelper
import mobile.substance.sdk.music.core.dataLinkers.MusicData
import mobile.substance.sdk.music.core.objects.Artist
import mobile.substance.sdk.music.core.objects.Playlist
import mobile.substance.sdk.music.core.objects.Song

fun Song.requestArt(imageView: ImageView) = MusicData.findAlbumById(songAlbumId!!)?.requestArt(imageView)

fun Artist.requestArt(imageView: ImageView) = Glide.with(imageView.context).load(artistImagePath).placeholder(R.drawable.placeholder_artist).crossFade().centerCrop().into(imageView)

fun Playlist.requestArt(imageView: ImageView) = async {
    Glide.with(imageView.context).load(await { MetadataHelper.getPath(imageView.context, id, Playlist::class.java.simpleName.toLowerCase()) }).placeholder(R.drawable.placeholder_playlist).crossFade().centerCrop().into(imageView)
}