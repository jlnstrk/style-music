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

package de.julianostarek.music.adapters

import android.content.Context
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import de.julianostarek.music.fragments.GenericMusicListFragment
import mobile.substance.sdk.music.loading.MusicType

class LibraryAdapter(val context: Context, fm: FragmentManager) : FragmentStatePagerAdapter(fm) {

    companion object {
        const val TAB_COUNT = 4
    }

    override fun getItem(position: Int): Fragment {
        when (position) {
            0 -> return GenericMusicListFragment(MusicType.SONGS)
            1 -> return GenericMusicListFragment(MusicType.ALBUMS)
            2 -> return GenericMusicListFragment(MusicType.ARTISTS)
            3 -> return GenericMusicListFragment(MusicType.PLAYLISTS)
            else -> return null as Fragment
        }
    }

    override fun getCount(): Int {
        return TAB_COUNT
    }

}