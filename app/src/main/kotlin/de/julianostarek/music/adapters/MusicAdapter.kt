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

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import de.julianostarek.music.anko.viewholders.UniversalViewHolder
import mobile.substance.sdk.music.core.objects.MediaObject
import org.jetbrains.anko.AnkoComponent
import org.jetbrains.anko.AnkoContext
import java.util.*

abstract class MusicAdapter<T : MediaObject> : RecyclerView.Adapter<UniversalViewHolder<T>>() {
    private var items: MutableList<T> = ArrayList()

    fun setData(items: List<T>?) {
        this.items = items.orEmpty().toMutableList()
    }

    fun addData(items: List<T>) {
        val oldSize = items.size
        this.items.addAll(items)
        notifyItemRangeInserted(oldSize, items.lastIndex)
    }

    open fun getData(): List<T> {
        return items
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): UniversalViewHolder<T> {
        val ui = getUI(viewType)
        return getViewHolder(ui.createView(AnkoContext.create(parent!!.context, parent)), ui, viewType)
    }

    abstract fun getViewHolder(view: View, ui: AnkoComponent<ViewGroup>, itemType: Int): UniversalViewHolder<T>

    abstract fun getUI(itemType: Int): AnkoComponent<ViewGroup>

    override fun getItemCount(): Int {
        return getData().size
    }

    override fun onBindViewHolder(holder: UniversalViewHolder<T>?, position: Int) {
        bindItem(getData()[position], holder!!, position)
    }

    abstract fun bindItem(item: T, holder: UniversalViewHolder<T>, position: Int)

}