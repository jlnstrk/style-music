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

package de.julianostarek.music.anko.fragments

import android.app.Activity
import android.content.res.Configuration
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CoordinatorLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.*
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import de.julianostarek.music.R
import de.julianostarek.music.adapters.LastAddedAdapter
import de.julianostarek.music.adapters.MostPlayedAdapter
import de.julianostarek.music.adapters.SongsSuggester
import de.julianostarek.music.anko.ListSection
import de.julianostarek.music.anko.viewholders.EmptyStateViewHolder
import de.julianostarek.music.anko.viewholders.UniversalViewHolder
import de.julianostarek.music.extensions.hasMostPlayedSongs
import de.julianostarek.music.extensions.setupMenu
import de.julianostarek.music.extensions.setupWithNavigationDrawer
import de.julianostarek.music.fragments.HomeFragment
import de.julianostarek.music.recyclerview.itemdecorations.ItemDecorations
import mobile.substance.sdk.music.core.dataLinkers.MusicData
import org.jetbrains.anko.*
import org.jetbrains.anko.appcompat.v7.titleResource
import org.jetbrains.anko.appcompat.v7.toolbar
import org.jetbrains.anko.design.appBarLayout
import org.jetbrains.anko.design.coordinatorLayout
import org.jetbrains.anko.recyclerview.v7.recyclerView

class HomeUI : AnkoComponent<HomeFragment> {
    lateinit var activity: AppCompatActivity
        private set
    lateinit var coordinatorLayout: CoordinatorLayout
        private set
    lateinit var appBarLayout: AppBarLayout
        private set
    lateinit var toolbar: Toolbar
        private set
    lateinit var recyclerView: RecyclerView
        private set
    var actionBarSize: Int = 0

    override fun createView(ui: AnkoContext<HomeFragment>) = with(ui) {
        activity = ctx as AppCompatActivity

        val tv = TypedValue()
        ctx.theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)
        actionBarSize = TypedValue.complexToDimensionPixelSize(tv.data, resources.displayMetrics)

        coordinatorLayout {
            coordinatorLayout = this
            layoutParams = ViewGroup.LayoutParams(matchParent, matchParent)
            appBarLayout = appBarLayout {
                stateListAnimator = null
                toolbar = toolbar {
                    titleResource = R.string.home
                    setupWithNavigationDrawer(ctx as Activity)
                    setupMenu(R.menu.menu_main, ctx as Activity)
                }.lparams(matchParent, actionBarSize) {
                    scrollFlags = AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
                }
            }.lparams(matchParent, wrapContent)
            recyclerView = recyclerView {
                bottomPadding = dip(56)
                clipToPadding = false
                layoutManager = LinearLayoutManager(ctx)
            }.lparams(matchParent, matchParent) {
                behavior = AppBarLayout.ScrollingViewBehavior()
            }
        }
    }

    inner class Adapter : RecyclerView.Adapter<UniversalViewHolder<*>>() {
        private var suggestSongs = false

        override fun onBindViewHolder(holder: UniversalViewHolder<*>?, position: Int) {
            if (itemCount == 1) {
                holder!!.ui.title?.text = activity.getString(R.string.no_music)
                holder.ui.image?.imageResource = R.drawable.empty_state_no_music
                return
            }
            return ListSection.handleBinding((holder as ListSection.ViewHolder?)!!, if (position == 0) if (suggestSongs) activity.getString(R.string.first_song) else activity.getString(R.string.most_played) else activity.getString(R.string.last_added),
                    if (position == 0) if (suggestSongs) null else activity.getString(R.string.more) else activity.getString(R.string.more), null, if (position == 0) {
                if (suggestSongs) {
                    (holder?.ui as ListSection.ViewHolder.UI).button.visibility = View.GONE
                    SongsSuggester(activity)
                } else MostPlayedAdapter(activity)
            } else LastAddedAdapter(activity))
        }

        override fun getItemCount(): Int {
            return if (MusicData.getSongs().isEmpty()) 1 else 2
        }

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): UniversalViewHolder<*> {
            val ui = if (viewType == 0) EmptyStateViewHolder.UI(0) else ListSection.ViewHolder.UI()
            return if (viewType == 0) {
                EmptyStateViewHolder(ui.createView(AnkoContext.create(activity, parent!!)), ui as EmptyStateViewHolder.UI, activity)
            } else ListSection.ViewHolder(ui.createView(AnkoContext.create(parent!!.context, parent)), ui as ListSection.ViewHolder.UI,
                    if (activity.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) activity.dip(12) + (activity.resources.displayMetrics.widthPixels - activity.dip(16)) / 3 * 2 else activity.dip(12) + (activity.resources.displayMetrics.widthPixels - activity.dip(24)) / 5 * 2,
                    if (viewType == 2 || viewType == 3) GridLayoutManager(activity, 2, GridLayoutManager.HORIZONTAL, false).apply {
                        spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                            override fun getSpanSize(position: Int): Int {
                                return if (viewType == 2 && position == 0 || viewType == 3 && position == 2) this@apply.spanCount else 1
                            }
                        }
                    } else LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false),
                    if (viewType == 2 || viewType == 3) ItemDecorations.ThreeItemsStaggeredGrid(activity, viewType == 3) else ItemDecorations.HorizontalLinearSpacing(activity), activity).apply {
                (ui as ListSection.ViewHolder.UI).button.visibility = View.GONE
            }
        }

        override fun getItemViewType(position: Int): Int {
            return if (position == 0) {
                if (!hasMostPlayedSongs(activity)) {
                    suggestSongs = true
                    1
                } else if (MusicData.getSongs().isEmpty()) {
                    suggestSongs = false
                    0
                } else {
                    suggestSongs = false
                    2
                }
            } else 3
        }

    }

}