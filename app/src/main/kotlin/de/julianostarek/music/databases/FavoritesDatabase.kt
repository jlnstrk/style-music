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

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper


class FavoritesDatabase(context: Context) : SQLiteOpenHelper(context, FavoritesDatabase.DATABASE_NAME, null, FavoritesDatabase.DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_ENTRIES)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(SQL_DELETE_ENTRIES)
        onCreate(db)
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }

    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "Favorites.db"

        private val SQL_CREATE_ENTRIES = DatabaseConstants.CREATE_TABLE +
                FavoritesContract.FavoritesEntry.TABLE_NAME +
                " (" +
                FavoritesContract.FavoritesEntry._ID +
                DatabaseConstants.PRIMARY_KEY +
                FavoritesContract.FavoritesEntry.COLUMN_NAME_MEDIA_ID +
                DatabaseConstants.INTEGER +
                DatabaseConstants.COMMA_SEP +
                FavoritesContract.FavoritesEntry.COLUMN_NAME_ITEM_TYPE +
                DatabaseConstants.TEXT_TYPE +
                " )"

        private val SQL_DELETE_ENTRIES = DatabaseConstants.DROP_TABLE_IF_EXISTS + FavoritesContract.FavoritesEntry.TABLE_NAME
    }
}
