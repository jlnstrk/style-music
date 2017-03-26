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

import android.app.Activity
import android.content.Intent
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.support.v7.widget.Toolbar
import de.julianostarek.music.R
import de.julianostarek.music.activities.MainActivity
import de.julianostarek.music.activities.SearchActivity
import mobile.substance.sdk.colors.ColorConstants

fun Toolbar.setupWithNavigationDrawer(activity: Activity) {
    navigationIcon = ContextCompat.getDrawable(activity, R.drawable.ic_menu_black_24dp).tinted(ColorConstants.ICON_COLOR_ACTIVE_LIGHT_BG)
    setNavigationOnClickListener { (activity as MainActivity).drawerLayout.openDrawer(GravityCompat.START) }
}

fun Toolbar.setupMenu(menuResId: Int, activity: Activity) {
    inflateMenu(menuResId)
    setOnMenuItemClickListener {
        when (it.itemId) {
            R.id.item_search -> {
                activity.startActivity(Intent(activity, SearchActivity::class.java), ActivityOptionsCompat.makeSceneTransitionAnimation(activity).toBundle())
                true
            }
            else -> false
        }
    }
}

