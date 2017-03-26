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
import android.content.Intent
import android.os.Debug
import android.support.design.widget.AppBarLayout
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.TabLayout
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewCompat
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import de.julianostarek.music.R
import de.julianostarek.music.activities.CreatePlaylistActivity
import de.julianostarek.music.adapters.LibraryAdapter
import de.julianostarek.music.extensions.setupMenu
import de.julianostarek.music.extensions.setupWithNavigationDrawer
import de.julianostarek.music.extensions.tinted
import de.julianostarek.music.fragments.GenericMusicListFragment
import de.julianostarek.music.fragments.LibraryFragment
import de.julianostarek.music.helper.AppColors
import mobile.substance.sdk.colors.ColorConstants
import mobile.substance.sdk.music.playback.PlaybackRemote
import org.jetbrains.anko.*
import org.jetbrains.anko.appcompat.v7.titleResource
import org.jetbrains.anko.appcompat.v7.toolbar
import org.jetbrains.anko.design.appBarLayout
import org.jetbrains.anko.design.coordinatorLayout
import org.jetbrains.anko.design.floatingActionButton
import org.jetbrains.anko.design.tabLayout
import org.jetbrains.anko.support.v4.viewPager

class LibraryUI : AnkoComponent<LibraryFragment>, ViewPager.OnPageChangeListener, TabLayout.OnTabSelectedListener, AppBarLayout.OnOffsetChangedListener {
    lateinit var activity: AppCompatActivity
        private set
    lateinit var appBarLayout: AppBarLayout
        private set
    lateinit var toolbar: Toolbar
        private set
    lateinit var tabLayout: TabLayout
        private set
    lateinit var viewPager: ViewPager
        private set
    lateinit var floatingActionButton: FloatingActionButton
        private set

    val drawableResIds = arrayOf(R.drawable.ic_music_note_outline_black_24dp, R.drawable.ic_album_black_24dp, R.drawable.ic_person_black_24dp, R.drawable.ic_queue_music_black_24dp)
    var actionBarSize: Int = 0

    override fun createView(ui: AnkoContext<LibraryFragment>) = with(ui) {
        activity = ctx as AppCompatActivity
        val tv = TypedValue()
        ctx.theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)
        actionBarSize = TypedValue.complexToDimensionPixelSize(tv.data, resources.displayMetrics)

        coordinatorLayout {
            appBarLayout = appBarLayout {
                stateListAnimator = null
                addOnOffsetChangedListener(this@LibraryUI)

                toolbar = toolbar {
                    titleResource = R.string.library
                    setupWithNavigationDrawer(ctx as Activity)
                    setupMenu(R.menu.menu_main, ctx as Activity)
                }.lparams(matchParent, actionBarSize) {
                    scrollFlags = AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
                }
                tabLayout = tabLayout {
                    tabGravity = TabLayout.GRAVITY_FILL
                    tabMode = TabLayout.MODE_FIXED

                    addOnTabSelectedListener(this@LibraryUI)
                }.lparams(matchParent, wrapContent) {
                    scrollFlags = 0
                }
            }.lparams(matchParent, wrapContent)
            viewPager = viewPager {
                // Id is required by some fragment stuff
                id = 1
                adapter = LibraryAdapter(ctx, (ctx as AppCompatActivity).supportFragmentManager)
                addOnPageChangeListener(this@LibraryUI)
            }.lparams(matchParent, matchParent) {
                behavior = AppBarLayout.ScrollingViewBehavior()
            }
            floatingActionButton = floatingActionButton {
                visibility = View.GONE
                imageResource = R.drawable.ic_add_white_24dp
                scaleX = 0F
                scaleY = 0F
                onClick { ctx.startActivity(Intent(ctx, CreatePlaylistActivity::class.java), ActivityOptionsCompat.makeSceneTransitionAnimation(ctx as Activity).toBundle()) }
                translationY = if (PlaybackRemote.isActive()) 0F else dip(56).toFloat()
            }.lparams(wrapContent, wrapContent) {
                gravity = Gravity.BOTTOM or Gravity.END
                bottomMargin = dip(72)
                marginEnd = dip(16)
            }

            // Late TabLayout initialization
            tabLayout.setupWithViewPager(viewPager)
            for (i in 0..3) {
                val tab = tabLayout.getTabAt(i)
                tab?.icon = ContextCompat.getDrawable(ctx, drawableResIds[i]).tinted(if (i == 0) AppColors.ACCENT_COLOR else ColorConstants.ICON_COLOR_ACTIVE_LIGHT_BG)
            }
        }
    }

    override fun onOffsetChanged(appBarLayout: AppBarLayout?, verticalOffset: Int) {
        if (verticalOffset == -actionBarSize && appBarLayout!!.elevation != activity.dip(4).toFloat()) {
            appBarLayout.elevation = activity.dip(4).toFloat()
        } else if (verticalOffset > -actionBarSize && appBarLayout!!.elevation == activity.dip(4).toFloat()) appBarLayout.elevation = 0F

    }

    override fun onTabReselected(tab: TabLayout.Tab?) {
    }

    override fun onTabUnselected(tab: TabLayout.Tab?) {
        tab?.icon?.setTint(ColorConstants.ICON_COLOR_ACTIVE_LIGHT_BG)
    }

    override fun onTabSelected(tab: TabLayout.Tab?) {
        tab?.icon?.setTint(AppColors.ACCENT_COLOR)
    }

    override fun onPageScrollStateChanged(state: Int) {

    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

    }

    override fun onPageSelected(position: Int) {
        if (((viewPager.adapter as LibraryAdapter).instantiateItem(viewPager, position) as GenericMusicListFragment).hasScrolled()) appBarLayout.setExpanded(false, true)
        when (position) {
            3 -> floatingActionButton.show()
            else -> if (floatingActionButton.visibility != View.GONE) floatingActionButton.hide()
        }
    }

}