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

package de.julianostarek.music.databases

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import de.julianostarek.music.lib.radio.BasicStation
import de.julianostarek.music.lib.radio.streamUrl
import de.julianostarek.music.provider.RadioStore
import de.julianostarek.music.types.DatabaseRadioStation
import de.julianostarek.music.types.PreInsertRadioStation
import java.util.*

object RadioHelper {

    fun parseSavedRadioStations(cursor: Cursor): List<DatabaseRadioStation> {
        if (cursor.moveToFirst()) {
            val stations = ArrayList<DatabaseRadioStation>()
            do {
                val name = cursor.getString(cursor.getColumnIndex(RadioContract.RadioEntry.COLUMN_NAME_STATION_TITLE))
                val genre = cursor.getString(cursor.getColumnIndex(RadioContract.RadioEntry.COLUMN_NAME_STATION_DESCRIPTION))
                val artworkPath = cursor.getString(cursor.getColumnIndex(RadioContract.RadioEntry.COLUMN_NAME_STATION_IMAGE_PATH))
                val streamUrl = cursor.getString(cursor.getColumnIndex(RadioContract.RadioEntry.COLUMN_NAME_STATION_STREAM_URL))
                val rowId = cursor.getLong(cursor.getColumnIndex(RadioContract.RadioEntry._ID))
                stations.add(DatabaseRadioStation(name, genre, artworkPath, streamUrl, rowId))
            } while (cursor.moveToNext())
            return stations

        } else return emptyList()
    }

    fun delete(context: Context, dbRowId: Long) {
        context.contentResolver.delete(RadioStore.CONTENT_URI, "${RadioContract.RadioEntry._ID} = ?", arrayOf(dbRowId.toString()))
    }

    fun contains(context: Context, stationId: Long): Long {
        context.contentResolver.query(RadioStore.CONTENT_URI, null, "${RadioContract.RadioEntry.COLUMN_NAME_STATION_STREAM_URL} = ?", arrayOf("http://stream.dar.fm/$stationId"), null).use {
            if (it != null && it.moveToFirst()) {
                return it.getLong(it.getColumnIndex(RadioContract.RadioEntry._ID))
            } else return -1
        }
    }

    fun updateStation(context: Context, station: DatabaseRadioStation): Int {
        return context.contentResolver.update(RadioStore.CONTENT_URI, ContentValues().apply {
            put(RadioContract.RadioEntry.COLUMN_NAME_STATION_TITLE, station.name)
            put(RadioContract.RadioEntry.COLUMN_NAME_STATION_DESCRIPTION, station.genre)
            put(RadioContract.RadioEntry.COLUMN_NAME_STATION_IMAGE_PATH, station.imagePath)
            put(RadioContract.RadioEntry.COLUMN_NAME_STATION_STREAM_URL, station.streamUrl)
        }, "${RadioContract.RadioEntry._ID} = ?", arrayOf(station.dbRowId.toString()))
    }

    fun insertDarFmStation(context: Context, station: PreInsertRadioStation): Long {
        return ContentUris.parseId(context.contentResolver.insert(RadioStore.CONTENT_URI, ContentValues().apply {
            put(RadioContract.RadioEntry.COLUMN_NAME_STATION_TITLE, station.name)
            put(RadioContract.RadioEntry.COLUMN_NAME_STATION_DESCRIPTION, station.genre)
            put(RadioContract.RadioEntry.COLUMN_NAME_STATION_IMAGE_PATH, station.imagePath)
            put(RadioContract.RadioEntry.COLUMN_NAME_STATION_STREAM_URL, station.streamUrl())
        }))
    }

    fun insertCustomStation(context: Context, title: String, description: String, imagePath: String?, streamUrl: String): Long {
        return ContentUris.parseId(context.contentResolver.insert(RadioStore.CONTENT_URI, ContentValues().apply {
            put(RadioContract.RadioEntry.COLUMN_NAME_STATION_TITLE, title)
            put(RadioContract.RadioEntry.COLUMN_NAME_STATION_DESCRIPTION, description)
            put(RadioContract.RadioEntry.COLUMN_NAME_STATION_IMAGE_PATH, imagePath)
            put(RadioContract.RadioEntry.COLUMN_NAME_STATION_STREAM_URL, streamUrl)
        }))
    }

}