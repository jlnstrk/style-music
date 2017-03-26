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

import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import co.metalab.asyncawait.async
import de.julianostarek.music.anko.viewholders.MostPlayedDetailViewHolder
import de.julianostarek.music.anko.viewholders.UniversalViewHolder
import de.julianostarek.music.extensions.getMostPlayed
import mobile.substance.sdk.music.core.dataLinkers.MusicData
import mobile.substance.sdk.music.core.objects.MediaObject
import org.jetbrains.anko.AnkoContext

class StatisticsDetailAdapter(private val activity: AppCompatActivity, private val type: Int) : RecyclerView.Adapter<UniversalViewHolder<*>>() {
    private var items: List<MediaObject> = emptyList()

    companion object {
        const val TYPE_MOST_PLAYED = 0
        const val TYPE_LAST_ADDED = 1
    }

    init {
        when (type) {
            TYPE_MOST_PLAYED -> async {
                items = await { getMostPlayed(activity) }
                notifyDataSetChanged()
            }
            TYPE_LAST_ADDED -> async {
                items = await { MusicData.getLastAddedObjects() }
                notifyDataSetChanged()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): UniversalViewHolder<*> {
        when (type) {
            TYPE_MOST_PLAYED -> {
                val ui = MostPlayedDetailViewHolder.UI()
                return MostPlayedDetailViewHolder<MediaObject>(ui.createView(AnkoContext.create(activity, parent!!)), ui, activity)
            }
            TYPE_LAST_ADDED -> {

            }
        }
        return null as UniversalViewHolder<*>
    }

    override fun onBindViewHolder(holder: UniversalViewHolder<*>?, position: Int) {

    }

    override fun getItemCount(): Int {
        return items.size
    }

}