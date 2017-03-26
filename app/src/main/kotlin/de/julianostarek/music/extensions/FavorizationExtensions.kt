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
import de.julianostarek.music.databases.DatabaseConstants
import de.julianostarek.music.databases.FavoritesContract
import de.julianostarek.music.databases.FavoritesDatabase
import mobile.substance.sdk.music.core.dataLinkers.MusicData
import mobile.substance.sdk.music.core.objects.MediaObject
import mobile.substance.sdk.music.loading.MusicType
import mobile.substance.sdk.utils.MusicCoreUtil
import java.util.*

fun <T : MediaObject> getFavorites(context: Context, type: MusicType, count: Int = 0, ignoreCountGetAll: Boolean = true): List<T> {
    val helper = FavoritesDatabase(context)
    val items = ArrayList<T>()
    val typeLowerCase = type.name.toLowerCase()
    helper.readableDatabase.query(FavoritesContract.FavoritesEntry.TABLE_NAME, null,
            "${FavoritesContract.FavoritesEntry.COLUMN_NAME_ITEM_TYPE} = ?",
            arrayOf(typeLowerCase.substring(0, typeLowerCase.length - 1)),
            null, null, null).use {
        if (it != null && it.moveToFirst()) {
            val allMedia = ArrayList<MediaObject>()
            when (type) {
                MusicType.SONGS -> allMedia.addAll(MusicData.getSongs())
                MusicType.ALBUMS -> allMedia.addAll(MusicData.getAlbums())
                MusicType.ARTISTS -> allMedia.addAll(MusicData.getArtists())
            }
            do {
                try {
                    val id = it.getInt(it.getColumnIndex(FavoritesContract.FavoritesEntry.COLUMN_NAME_MEDIA_ID)).toLong()
                    val item = MusicCoreUtil.findByMediaId(id, allMedia)
                    if (item != null)
                        items.add(item as T)
                    if (items.size == count && !ignoreCountGetAll)
                        break
                } catch (e: Exception) {
                }
            } while (it.moveToNext())
        }
    }
    helper.close()
    return items
}

fun <T : MediaObject> T.isFavorite(context: Context): Boolean {
    val helper = FavoritesDatabase(context)

    val cursor = helper.readableDatabase.query(FavoritesContract.FavoritesEntry.TABLE_NAME, null, "${FavoritesContract.FavoritesEntry.COLUMN_NAME_MEDIA_ID} = ?", arrayOf(id.toString()), null, null, null)
    try {
        return cursor != null && cursor.moveToFirst()
    } finally {
        helper.close()
        cursor?.close()
    }
}


fun <T : MediaObject> T.setFavorite(context: Context, favorite: Boolean): Boolean {
    try {
        if (isFavorite(context) != favorite) {
            val helper = FavoritesDatabase(context)
            if (favorite) {
                val cv = ContentValues()
                cv.put(FavoritesContract.FavoritesEntry.COLUMN_NAME_ITEM_TYPE, this::class.java.simpleName.toLowerCase())
                cv.put(FavoritesContract.FavoritesEntry.COLUMN_NAME_MEDIA_ID, id.toInt())
                helper.writableDatabase.insert(FavoritesContract.FavoritesEntry.TABLE_NAME, DatabaseConstants.NULL_COLUMN_HACK, cv)
                helper.close()
                return favorite
            } else {
                helper.writableDatabase.delete(FavoritesContract.FavoritesEntry.TABLE_NAME, "${FavoritesContract.FavoritesEntry.COLUMN_NAME_MEDIA_ID} = ?", arrayOf(id.toString()))
                helper.close()
                return favorite
            }
        } else return favorite
    } finally {
        context.sendBroadcast(Intent("de.julianostarek.music.favorites.${this::class.java.simpleName.toLowerCase()}s.CHANGE"))
    }
}