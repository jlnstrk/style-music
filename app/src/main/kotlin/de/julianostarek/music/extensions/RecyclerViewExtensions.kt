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

import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView


fun <T : RecyclerView.ViewHolder> T.isCompletelyVisible(): Boolean {
    val layoutManager = (itemView.parent as RecyclerView).layoutManager
    val position = adapterPosition
    when (layoutManager) {
        is LinearLayoutManager -> return layoutManager.findFirstCompletelyVisibleItemPosition() <= position && layoutManager.findLastCompletelyVisibleItemPosition() >= position
        is GridLayoutManager -> return layoutManager.findFirstCompletelyVisibleItemPosition() <= position && layoutManager.findLastCompletelyVisibleItemPosition() >= position
    }
    return true
}

fun <T : RecyclerView.ViewHolder> T.isVisible(): Boolean {
    val layoutManager = (itemView.parent as RecyclerView).layoutManager
    val position = adapterPosition
    when (layoutManager) {
        is LinearLayoutManager -> return layoutManager.findFirstVisibleItemPosition() <= position && layoutManager.findLastVisibleItemPosition() >= position
        is GridLayoutManager -> return layoutManager.findFirstVisibleItemPosition() <= position && layoutManager.findLastVisibleItemPosition() >= position
    }
    return true
}