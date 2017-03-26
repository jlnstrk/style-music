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

package de.julianostarek.music.types

import android.net.Uri
import de.julianostarek.music.lib.radio.BasicStation
import de.julianostarek.music.lib.radio.streamUrl
import mobile.substance.sdk.music.core.objects.Song

class PreInsertRadioStation(name: String, genre: String, id: Long, val imagePath: String?) : BasicStation(name, genre, id)

data class DatabaseRadioStation(val name: String, val genre: String, val imagePath: String?, val streamUrl: String? = null, val dbRowId: Long = 0)

fun DatabaseRadioStation.toSong(): Song {
    val song = Song.Builder()
            .setId(dbRowId)
            .setTitle(name)
            .setArtistName(genre)
            .build()
    song.explicitUri = Uri.parse(streamUrl)
    song.explicitArtworkUri = Uri.parse("file://" + imagePath)
    song.putData("is_radio", true)
    return song
}