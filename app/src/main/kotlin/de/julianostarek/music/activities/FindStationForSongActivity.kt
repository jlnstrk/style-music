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

import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.design.widget.TextInputEditText
import android.support.design.widget.TextInputLayout
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import butterknife.bindView
import butterknife.bindViews
import co.metalab.asyncawait.async
import de.julianostarek.music.BuildConfig
import de.julianostarek.music.R
import de.julianostarek.music.lib.radio.RadioAPI
import de.julianostarek.music.lib.radio.streamUrl
import de.julianostarek.music.types.DatabaseRadioStation
import de.julianostarek.music.types.toSong
import mobile.substance.sdk.music.playback.PlaybackRemote

class FindStationForSongActivity : PlaybackRemoteActivity() {
    private val toolbar: Toolbar by bindView<Toolbar>(R.id.activity_find_station_for_song_toolbar)
    private val editTexts: List<TextInputEditText> by bindViews<TextInputEditText>(R.id.activity_find_station_for_song_name_edit_text, R.id.activity_find_station_for_song_artist_edit_text)
    private val search: FloatingActionButton by bindView<FloatingActionButton>(R.id.activity_find_station_for_song_search)
    private val description: TextView by bindView<TextView>(R.id.activity_find_station_for_song_description)
    private var isListening: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_station_for_song)

        Toast.makeText(this, "This is still experimental!", Toast.LENGTH_LONG).show()

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = null

        search.setOnClickListener {
            if (editTexts.any { it.text.toString().isEmpty() || it.text.toString().isBlank() }) {
                editTexts.forEach {
                    if (it.text.toString().isEmpty() || it.text.toString().isBlank()) {
                        ((it.parent as FrameLayout).parent as TextInputLayout).error = getString(R.string.must_not_be_empty)
                    }
                }
            } else async {
                val result = await { RadioAPI(BuildConfig.DARFM_KEY).getStationsMatchingMetadata(editTexts[0].text.toString(), editTexts[1].text.toString(), false) }
                if (result == null || result.isEmpty()) {
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.no_stations_found), Snackbar.LENGTH_SHORT).show()
                } else {
                    val firstResult = result.first()
                    isListening = true
                    val wasActive = PlaybackRemote.isActive()
                    PlaybackRemote.play(DatabaseRadioStation(firstResult.name, firstResult.genre, null, firstResult.streamUrl()).toSong())
                    if (!wasActive) window.sharedElementReturnTransition = null
                    supportFinishAfterTransition()
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == android.R.id.home) {
            supportFinishAfterTransition()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

}