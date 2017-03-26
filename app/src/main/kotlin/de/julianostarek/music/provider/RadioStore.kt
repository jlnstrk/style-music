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

package de.julianostarek.music.provider

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import de.julianostarek.music.databases.RadioContract
import de.julianostarek.music.databases.RadioDatabase

class RadioStore : ContentProvider() {

    companion object {
        val CONTENT_URI: Uri = Uri.parse("content://de.julianostarek.music.radio/")
    }

    override fun insert(uri: Uri?, values: ContentValues?): Uri {
        val db = RadioDatabase(context)
        try {
            return ContentUris.withAppendedId(CONTENT_URI, db.writableDatabase.insert(RadioContract.RadioEntry.TABLE_NAME, "null", values))
        } finally {
            context.contentResolver.notifyChange(CONTENT_URI, null)
            db.close()
        }
    }

    override fun query(uri: Uri?, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor {
        val cursor = RadioDatabase(context).readableDatabase.query(RadioContract.RadioEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder)
        cursor.setNotificationUri(context.contentResolver, CONTENT_URI)
        return cursor
    }

    override fun onCreate(): Boolean {
        return true
    }

    override fun update(uri: Uri?, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int {
        val db = RadioDatabase(context)
        try {
            return db.writableDatabase.update(RadioContract.RadioEntry.TABLE_NAME, values, selection, selectionArgs)
        } finally {
            context.contentResolver.notifyChange(CONTENT_URI, null)
            db.close()
        }
    }

    override fun delete(uri: Uri?, selection: String?, selectionArgs: Array<out String>?): Int {
        val db = RadioDatabase(context)
        try {
            return db.writableDatabase.delete(RadioContract.RadioEntry.TABLE_NAME, selection, selectionArgs)
        } finally {
            context.contentResolver.notifyChange(CONTENT_URI, null)
            db.close()
        }
    }

    override fun getType(uri: Uri?): String {
        return "0"
    }

}