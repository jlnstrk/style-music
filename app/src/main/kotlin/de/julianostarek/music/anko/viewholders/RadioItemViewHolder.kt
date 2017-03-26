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
import de.julianostarek.music.lib.radio.BasicStation
import org.jetbrains.anko.*

class RadioItemViewHolder(itemView: View, ui: UI, activity: AppCompatActivity) : UniversalViewHolder<BasicStation>(itemView, ui, activity) {

    class UI : UniversalUI() {

        override fun createView(ui: AnkoContext<ViewGroup>) = with(ui) {
            frameLayout {
                layoutParams = ViewGroup.LayoutParams(matchParent, dip(72))
                title = textView {
                    bottomPadding = dip(20)
                    singleLine = true
                }.lparams(wrapContent, wrapContent) {
                    gravity = Gravity.CENTER_VERTICAL
                    rightMargin = dip(24)
                    leftMargin = dip(80)
                }
                subtitle = textView {
                    topPadding = dip(20)
                    singleLine = true
                }.lparams(wrapContent, wrapContent) {
                    gravity = Gravity.CENTER_VERTICAL
                    rightMargin = dip(24)
                    leftMargin = dip(80)
                }
                image = imageView {
                    adjustViewBounds = true
                }.lparams(dip(40), dip(40)) {
                    leftMargin = dip(24)
                    gravity = Gravity.CENTER_VERTICAL
                }
            }
        }

    }

}