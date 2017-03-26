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
import android.view.View
import android.view.ViewGroup
import org.jetbrains.anko.*
import org.jetbrains.anko.recyclerview.v7.recyclerView

class NestedRecyclerView(itemView: View, val ui: UI) : RecyclerView.ViewHolder(itemView) {

    class UI : AnkoComponent<ViewGroup> {
        lateinit var recyclerView: RecyclerView
            private set

        override fun createView(ui: AnkoContext<ViewGroup>) = with(ui) {
            recyclerView {
                recyclerView = this
                isNestedScrollingEnabled = false
                layoutParams = ViewGroup.LayoutParams(matchParent, dip(192))
                leftPadding = dip(72)
                rightPadding = dip(16)
                clipToPadding = false
            }
        }

    }

}