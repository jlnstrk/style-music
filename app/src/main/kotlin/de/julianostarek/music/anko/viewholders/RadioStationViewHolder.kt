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

package de.julianostarek.music.anko.viewholders

import android.support.v7.app.AppCompatActivity
import android.view.View
import com.afollestad.materialdialogs.MaterialDialog
import de.julianostarek.music.R
import de.julianostarek.music.databases.RadioContract
import de.julianostarek.music.fragments.ModifyStationDialog
import de.julianostarek.music.provider.RadioStore
import de.julianostarek.music.types.DatabaseRadioStation
import de.julianostarek.music.types.toSong
import mobile.substance.sdk.music.playback.PlaybackRemote


class RadioStationViewHolder(itemView: View, ui: ArtistViewHolder.UI, activity: AppCompatActivity) : ColorableViewHolder<DatabaseRadioStation>(itemView, ui, activity), View.OnLongClickListener {

    init {
        itemView.setOnLongClickListener(this)
    }

    override fun onClick(v: View?) {
        PlaybackRemote.play(item!!.toSong())
    }

    override fun onLongClick(v: View?): Boolean {
        ModifyStationDialog.showWith(item, activity.supportFragmentManager)
        return true
    }

}