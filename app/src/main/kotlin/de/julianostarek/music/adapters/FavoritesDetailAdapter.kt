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

import android.support.v4.view.ViewCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import co.metalab.asyncawait.async
import de.julianostarek.music.anko.viewholders.ImageViewHolders
import de.julianostarek.music.anko.viewholders.SongViewHolders
import de.julianostarek.music.anko.viewholders.UniversalViewHolder
import de.julianostarek.music.extensions.getFavorites
import mobile.substance.sdk.music.core.objects.Album
import mobile.substance.sdk.music.core.objects.Artist
import mobile.substance.sdk.music.core.objects.MediaObject
import mobile.substance.sdk.music.core.objects.Song
import mobile.substance.sdk.music.loading.MusicType
import org.jetbrains.anko.AnkoComponent
import org.jetbrains.anko.AnkoContext

class FavoritesDetailAdapter(private val type: MusicType, private val activity: AppCompatActivity) : RecyclerView.Adapter<UniversalViewHolder<*>>() {
    private var items: List<MediaObject> = emptyList()

    init {
        when (type) {
            MusicType.SONGS -> async {
                items = await { getFavorites(activity, MusicType.SONGS) }
                notifyDataSetChanged()
            }
            MusicType.ALBUMS -> async {
                items = await { getFavorites(activity, MusicType.ALBUMS) }
                notifyDataSetChanged()
            }
            MusicType.ARTISTS -> async {
                items = await { getFavorites(activity, MusicType.ARTISTS) }
                notifyDataSetChanged()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): UniversalViewHolder<*> {
        val ui = getUI()
        val view = ui.createView(AnkoContext.create(activity, parent!!))
        when (type) {
            MusicType.SONGS -> return SongViewHolders.Favorite(view, ui as SongViewHolders.Favorite.UI, activity)
            MusicType.ALBUMS -> return ImageViewHolders.Vertical<Album>(view, ui as ImageViewHolders.Vertical.UI, activity)
            MusicType.ARTISTS -> return ImageViewHolders.Vertical<Artist>(view, ui as ImageViewHolders.Vertical.UI, activity)
            else -> return null as UniversalViewHolder<*>
        }
    }

    private fun getUI(): AnkoComponent<ViewGroup> {
        when (type) {
            MusicType.SONGS -> return SongViewHolders.Favorite.UI()
            MusicType.ALBUMS,
            MusicType.ARTISTS -> return ImageViewHolders.Vertical.UI()
            else -> return null as AnkoComponent<ViewGroup>
        }
    }

    override fun onBindViewHolder(holder: UniversalViewHolder<*>?, position: Int) {
        val item = items[position]
        ViewCompat.setTransitionName(holder?.itemView, position.toString())

        when (item) {
            is Song -> (holder as UniversalViewHolder<Song>).item = item
            is Album -> (holder as UniversalViewHolder<Album>).item = item
            is Artist -> (holder as UniversalViewHolder<Artist>).item = item
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

}