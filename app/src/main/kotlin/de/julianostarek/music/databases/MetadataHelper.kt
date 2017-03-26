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

import android.content.ContentValues
import android.content.Context
import android.support.v7.media.MediaItemMetadata
import mobile.substance.sdk.music.core.objects.MediaObject

object MetadataHelper {

    fun contains(context: Context, mediaId: Long, itemType: String): Boolean {
        val db = MetadataDatabase(context)
        val cursor = db.readableDatabase.query(MetadataContract.MetadataEntry.TABLE_NAME, null, "${MetadataContract.MetadataEntry.COLUMN_NAME_MEDIA_ID} = ? AND ${MetadataContract.MetadataEntry.COLUMN_NAME_ITEM_TYPE} = ?", arrayOf(mediaId.toString(), itemType), null, null, null)
        try {
            return cursor != null && cursor.moveToFirst()
        } finally {
            cursor.close()
            db.close()
        }
    }

    fun insert(context: Context, mediaId: Long, path: String, itemType: String) {
        val db = MetadataDatabase(context)

        val cv = ContentValues()

        cv.put(MetadataContract.MetadataEntry.COLUMN_NAME_PATH, path)

        if (contains(context, mediaId, itemType)) {
            db.writableDatabase.update(MetadataContract.MetadataEntry.TABLE_NAME, cv, "${MetadataContract.MetadataEntry.COLUMN_NAME_MEDIA_ID} = ? AND ${MetadataContract.MetadataEntry.COLUMN_NAME_ITEM_TYPE} = ?", arrayOf(mediaId.toString(), itemType))
        } else {
            cv.put(MetadataContract.MetadataEntry.COLUMN_NAME_MEDIA_ID, mediaId.toInt())
            cv.put(MetadataContract.MetadataEntry.COLUMN_NAME_ITEM_TYPE, itemType)
            db.writableDatabase.insert(MetadataContract.MetadataEntry.TABLE_NAME, "null", cv)
        }
        db.close()
    }

    fun delete(context: Context, mediaId: Long, itemType: String) {
        val db = MetadataDatabase(context)
        db.writableDatabase.delete(MetadataContract.MetadataEntry.TABLE_NAME, "${MetadataContract.MetadataEntry.COLUMN_NAME_MEDIA_ID} = ? AND ${MetadataContract.MetadataEntry.COLUMN_NAME_ITEM_TYPE} = ?", arrayOf(mediaId.toString(), itemType))
    }

    fun getPath(context: Context, mediaId: Long, itemType: String): String? {
        val db = MetadataDatabase(context)

        val cursor = db.readableDatabase.query(MetadataContract.MetadataEntry.TABLE_NAME, null, "${MetadataContract.MetadataEntry.COLUMN_NAME_MEDIA_ID} = ? AND ${MetadataContract.MetadataEntry.COLUMN_NAME_ITEM_TYPE} = ?", arrayOf(mediaId.toString(), itemType), null, null, null)

        var path: String? = null

        if (cursor != null && cursor.moveToFirst()) path = cursor.getString(cursor.getColumnIndex(MetadataContract.MetadataEntry.COLUMN_NAME_PATH))

        try {
            return path
        } finally {
            cursor?.close()
            db.close()
        }
    }

    fun deleteAllOfType(context: Context, itemType: String) {
        val db = MetadataDatabase(context)
        db.writableDatabase.delete(MetadataContract.MetadataEntry.TABLE_NAME, "${MetadataContract.MetadataEntry.COLUMN_NAME_ITEM_TYPE} = ?", arrayOf(itemType))
    }

}