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
import com.bumptech.glide.Glide
import de.julianostarek.music.R
import de.julianostarek.music.anko.fragments.RadioUI
import de.julianostarek.music.anko.viewholders.RadioItemViewHolder
import de.julianostarek.music.lib.radio.BasicStation
import de.julianostarek.music.lib.radio.RadioAPI
import de.julianostarek.music.types.BasicStationSuggestion
import org.jetbrains.anko.AnkoContext

class RadioSearchResultsAdapter(private val results: List<BasicStation>, private val api: RadioAPI, private val ui: RadioUI, private val activity: AppCompatActivity) : RecyclerView.Adapter<RadioItemViewHolder>() {

    override fun onBindViewHolder(holder: RadioItemViewHolder?, position: Int) {
        val item = results[position]
        holder?.ui?.title?.text = item.name
        holder?.ui?.subtitle?.text = item.genre
        Glide.clear(holder!!.ui.image)
        async {
            val result = await { api.getStationDetails(item, false) }
            Glide.with(holder.ui.image?.context).load(result?.imageUrl).placeholder(R.drawable.placeholder_album).crossFade().into(holder.ui.image)
        }
        holder.itemView?.setOnClickListener { ui.onSuggestionClicked(BasicStationSuggestion(item.name, item.genre, item.id)) }
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RadioItemViewHolder {
        val ui = RadioItemViewHolder.UI()
        return RadioItemViewHolder(ui.createView(AnkoContext.create(parent!!.context, parent)), ui, activity)
    }

    override fun getItemCount(): Int {
        return results.size
    }
}