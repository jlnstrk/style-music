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

package de.julianostarek.music.anko.viewholders

import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import de.julianostarek.music.R
import de.julianostarek.music.anko.constraintLayout
import org.jetbrains.anko.*

class EmptyStateViewHolder(view: View, ui: UI, activity: AppCompatActivity) : UniversalViewHolder<Nothing>(view, ui, activity) {

    class UI(private val paddingBottom: Int) : UniversalUI() {

        override fun createView(ui: AnkoContext<ViewGroup>) = with(ui) {
            frameLayout {
                layoutParams = ViewGroup.LayoutParams(matchParent, matchParent)
                bottomPadding = this@UI.paddingBottom
                image = imageView().lparams(matchParent, matchParent)
                title = textView {
                    setTextAppearance(ctx, R.style.TextAppearance_AppCompat_Title)
                    gravity = Gravity.CENTER_HORIZONTAL
                }.lparams(matchParent, wrapContent) {
                    topMargin = dip(56)
                }
            }

        }

    }

}