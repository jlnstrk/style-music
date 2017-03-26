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

package de.julianostarek.music.anko

import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import de.julianostarek.music.R
import de.julianostarek.music.activities.FavoritesDetailActivity
import org.jetbrains.anko.*
import org.jetbrains.anko.cardview.v7.cardView
import org.jetbrains.anko.recyclerview.v7.recyclerView

class GenericDetailUI : AnkoComponent<FavoritesDetailActivity> {
    lateinit var title: TextView
        private set
    lateinit var close: ImageView
        private set
    lateinit var recyclerView: RecyclerView
        private set
    lateinit var container: View
        private set

    override fun createView(ui: AnkoContext<FavoritesDetailActivity>) = with(ui) {
        frameLayout {
            val ta = ctx.obtainStyledAttributes(arrayOf(android.R.attr.actionBarSize, android.R.attr.selectableItemBackgroundBorderless).toIntArray())
            container = cardView {
                transitionName = ctx.getString(R.string.transition_name_background)
                isClickable = true
                radius = 0F

                close = imageView {
                    id = 1
                    translationX = dip(-56).toFloat()
                    padding = dip(12)
                    isClickable = true
                    background = ta.getDrawable(1)
                    imageResource = R.drawable.ic_close_black_transparent_24dp
                }.lparams(dip(48), dip(48)) {
                    leftMargin = dip(4)
                }
                title = textView {
                    transitionName = ctx.getString(R.string.transition_name_title)
                    setTextAppearance(context, R.style.ItemTitleTextAppearance)
                    horizontalPadding = dip(16)
                    gravity = Gravity.CENTER_VERTICAL
                }.lparams(wrapContent, dip(48)) {
                    leftMargin = dip(56)
                }
                recyclerView = recyclerView().lparams(matchParent, wrapContent) {
                    topMargin = dip(48)
                }
            }.lparams(matchParent, wrapContent) {
                gravity = Gravity.CENTER_VERTICAL
            }
            ta.recycle()
        }
    }

}