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

package de.julianostarek.music.extensions

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.Cursor
import de.julianostarek.music.databases.DatabaseConstants
import de.julianostarek.music.databases.PlaybackStatisticsContract
import de.julianostarek.music.databases.PlaybackStatisticsDatabase
import mobile.substance.sdk.music.core.dataLinkers.MusicData
import mobile.substance.sdk.music.core.objects.MediaObject
import mobile.substance.sdk.utils.MusicCoreUtil
import java.util.*
import kotlin.concurrent.thread

fun <T : MediaObject> T.addPlayed(context: Context) = thread {
    try {
        val helper = PlaybackStatisticsDatabase(context)
        var oldVal: Int? = null
        var cursor: Cursor? = null
        try {
            cursor = helper.readableDatabase.query(PlaybackStatisticsContract.PlaybackStatisticsEntry.TABLE_NAME, null, "${PlaybackStatisticsContract.PlaybackStatisticsEntry.COLUMN_NAME_MEDIA_ID} = ?", arrayOf(id.toString()), null, null, null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (cursor != null) {
            if (!cursor.moveToFirst()) {
                cursor.close()
            } else {
                oldVal = cursor.getInt(cursor.getColumnIndex(PlaybackStatisticsContract.PlaybackStatisticsEntry.COLUMN_NAME_PLAYED_VALUE))
                cursor.close()
            }
        }

        val cv = ContentValues()
        cv.put(PlaybackStatisticsContract.PlaybackStatisticsEntry.COLUMN_NAME_PLAYED_VALUE, (oldVal ?: 0) + 1)

        if (oldVal == null) {
            cv.put(PlaybackStatisticsContract.PlaybackStatisticsEntry.COLUMN_NAME_MEDIA_ID, id)
            helper.writableDatabase.insert(PlaybackStatisticsContract.PlaybackStatisticsEntry.TABLE_NAME, DatabaseConstants.NULL_COLUMN_HACK, cv)
        } else helper.writableDatabase.update(PlaybackStatisticsContract.PlaybackStatisticsEntry.TABLE_NAME, cv, "${PlaybackStatisticsContract.PlaybackStatisticsEntry.COLUMN_NAME_MEDIA_ID} = ?", arrayOf(id.toString()))
        context.sendBroadcast(Intent("de.julianostarek.music.most_played.CHANGE"))
        helper.close()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun getMostPlayed(context: Context): List<MediaObject> {
    val db = PlaybackStatisticsDatabase(context)
    val cursor = db.readableDatabase.query(PlaybackStatisticsContract.PlaybackStatisticsEntry.TABLE_NAME, null, null, null, null, null, PlaybackStatisticsContract.PlaybackStatisticsEntry.COLUMN_NAME_PLAYED_VALUE + " DESC")

    try {
        val objs = ArrayList<MediaObject>()

        val allMedia = ArrayList<MediaObject>()
        allMedia.addAll(MusicData.getSongs())
        allMedia.addAll(MusicData.getAlbums())
        allMedia.addAll(MusicData.getPlaylists())

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    try {
                        val newObj = MusicCoreUtil.findByMediaId(cursor.getInt(cursor.getColumnIndex(PlaybackStatisticsContract.PlaybackStatisticsEntry.COLUMN_NAME_MEDIA_ID)).toLong(), allMedia) ?: continue
                        objs.add(newObj)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } while (cursor.moveToNext())
                cursor.close()
            }
        }
        return objs
    } finally {
        cursor?.close()
        db.close()
    }
}

fun hasMostPlayedSongs(context: Context): Boolean {
    val db = PlaybackStatisticsDatabase(context)
    try {
        db.readableDatabase.query(PlaybackStatisticsContract.PlaybackStatisticsEntry.TABLE_NAME, null, null, null, null, null, null).use {
            return it != null && it.moveToFirst()
        }
    } finally {
        db.close()
    }
}