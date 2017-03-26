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

import android.database.Cursor
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import co.metalab.asyncawait.async
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import de.julianostarek.music.R
import de.julianostarek.music.anko.ListHeader
import de.julianostarek.music.anko.viewholders.*
import de.julianostarek.music.databases.RadioHelper
import de.julianostarek.music.types.DatabaseRadioStation
import mobile.substance.sdk.colors.ColorPackage
import mobile.substance.sdk.colors.DynamicColors
import mobile.substance.sdk.colors.DynamicColorsCallback
import mobile.substance.sdk.options.DynamicColorsOptions
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.dip

class RadioStationsAdapter(private val activity: AppCompatActivity) : RecyclerView.Adapter<UniversalViewHolder<*>>() {
    private var stations: List<DatabaseRadioStation>? = null

    fun onDataSetChanged(cursor: Cursor) = async {
        stations = await { RadioHelper.parseSavedRadioStations(cursor) }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): UniversalViewHolder<*> {
        if (viewType == 2) {
            val ui = EmptyStateViewHolder.UI(activity.dip(64))
            return EmptyStateViewHolder(ui.createView(AnkoContext.create(parent!!.context, parent)), ui, activity)
        }
        if (viewType == 1) {
            val ui = ListHeader.UI()
            return ListHeader(ui.createView(AnkoContext.create(parent!!.context, parent)), ui, activity)
        }
        val ui = ArtistViewHolder.UI()
        return RadioStationViewHolder(ui.createView(AnkoContext.create(parent!!.context, parent)), ui, activity)
    }

    override fun getItemCount(): Int {
        return (stations?.size ?: 0) + 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) if (itemCount == 1) 2 else 1 else 0
    }

    override fun onBindViewHolder(holder: UniversalViewHolder<*>?, position: Int) {
        if (position == 0) {
            if (stations.orEmpty().isEmpty()) holder?.ui?.image?.setImageResource(R.drawable.empty_state_radio) else holder?.ui?.title?.text = activity.getString(R.string.saved)
        } else {
            val item = stations!![position - 1]
            (holder as RadioStationViewHolder).item = item
            holder.ui.title?.text = item.name
            Glide.with(holder.ui.image?.context).load(item.imagePath).placeholder(R.drawable.placeholder_album).diskCacheStrategy(DiskCacheStrategy.NONE).crossFade().centerCrop().into(holder.ui.image)
            if (item.imagePath != null && item.imagePath != "null") {
                DynamicColors.from(item.imagePath.orEmpty()).generate(true, object : DynamicColorsCallback {
                    override fun onColorsReady(colors: ColorPackage) {
                        (holder.ui as ColorableViewHolder.ColorableUI).colorable.setBackgroundColor(colors.primaryColor)
                        holder.ui.title?.setTextColor(colors.textColor)
                    }
                })
            } else {
                val colors = DynamicColorsOptions.defaultColors
                (holder.ui as ColorableViewHolder.ColorableUI).colorable.setBackgroundColor(colors.primaryColor)
                holder.ui.title?.setTextColor(colors.textColor)
            }
        }
    }
}