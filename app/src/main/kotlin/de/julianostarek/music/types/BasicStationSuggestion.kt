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

import android.os.Parcel
import android.os.Parcelable
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion
import de.julianostarek.music.lib.radio.BasicStation

class BasicStationSuggestion(name: String, genre: String, id: Long) : BasicStation(name, genre, id), Parcelable, SearchSuggestion {

    override fun writeToParcel(p0: Parcel?, p1: Int) {
        p0!!.writeString(name)
        p0.writeString(genre)
        p0.writeLong(id)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun getBody(): String {
        return name
    }

    constructor(source: Parcel) : this(source.readString(), source.readString(), source.readLong())

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<BasicStationSuggestion> = object : Parcelable.Creator<BasicStationSuggestion> {
            override fun newArray(p0: Int): Array<out BasicStationSuggestion> {
                return emptyArray()
            }

            override fun createFromParcel(p0: Parcel?): BasicStationSuggestion {
                return BasicStationSuggestion(p0!!)
            }

        }

        fun parse(station: BasicStation): BasicStationSuggestion {
            return BasicStationSuggestion(station.name, station.genre, station.id)
        }
    }
}