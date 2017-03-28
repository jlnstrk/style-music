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

import android.content.res.Configuration
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.view.ViewTreeObserver
import de.julianostarek.music.R
import de.julianostarek.music.adapters.FavoritesDetailAdapter
import de.julianostarek.music.anko.GenericDetailUI
import de.julianostarek.music.recyclerview.itemdecorations.ItemDecorations
import mobile.substance.sdk.music.loading.MusicType
import org.jetbrains.anko.AnkoContext

class FavoritesDetailActivity : AppCompatActivity() {
    lateinit var UI: GenericDetailUI
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        UI = GenericDetailUI()
        UI.createView(AnkoContext.create(this, this, true))
        supportPostponeEnterTransition()
        init()
        when (intent.action) {
            "songs" -> initRecyclerView(MusicType.SONGS)
            "albums" -> initRecyclerView(MusicType.ALBUMS)
            "artists" -> initRecyclerView(MusicType.ARTISTS)
        }
    }

    private fun init() {
        UI.title.text = getString(R.string.favorites)
        UI.close.setOnClickListener { supportFinishAfterTransition() }
    }

    private fun initRecyclerView(type: MusicType) {
        UI.recyclerView.apply {
            layoutManager = GridLayoutManager(this@FavoritesDetailActivity, if (type == MusicType.SONGS) if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) 2 else 3 else if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) 3 else 4)
            addItemDecoration(ItemDecorations.VerticalGridSpacing(this@FavoritesDetailActivity, if (type == MusicType.SONGS) 2 else 3, includeEdge = true))
            adapter = FavoritesDetailAdapter(type, this@FavoritesDetailActivity)
            viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    viewTreeObserver.removeOnPreDrawListener(this)
                    supportStartPostponedEnterTransition()
                    return true
                }
            })
        }
    }

}