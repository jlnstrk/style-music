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
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import de.julianostarek.music.anko.viewholders.AlbumViewHolder
import mobile.substance.sdk.music.core.dataLinkers.MusicData
import org.jetbrains.anko.AnkoContext

class SongsSuggester(private val context: Context) : RecyclerView.Adapter<AlbumViewHolder.AsSong>() {

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): AlbumViewHolder.AsSong {
        val ui = AlbumViewHolder.AsSong.UI()
        return AlbumViewHolder.AsSong(ui.createView(AnkoContext.create(context, parent!!)), ui, context as AppCompatActivity)
    }

    override fun onBindViewHolder(holder: AlbumViewHolder.AsSong?, position: Int) {
        (holder as AlbumViewHolder.AsSong).item = MusicData.getSongs()[position]
    }

    override fun getItemCount(): Int {
        return MusicData.getSongs().size
    }

}