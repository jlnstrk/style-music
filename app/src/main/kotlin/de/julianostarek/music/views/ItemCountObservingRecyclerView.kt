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

package de.julianostarek.music.views

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet

class ItemCountObservingRecyclerView : RecyclerView {
    var callback: ((Boolean) -> Any)? = null
    private val observer = object : RecyclerView.AdapterDataObserver() {
        override fun onChanged() = invalidateIsEmpty()

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) = invalidateIsEmpty()

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) = invalidateIsEmpty()
    }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private fun invalidateIsEmpty() {
        if (adapter != null) callback?.invoke(adapter.itemCount == 0)
    }

    override fun setAdapter(adapter: RecyclerView.Adapter<*>?) {
        this.adapter?.unregisterAdapterDataObserver(observer)
        super.setAdapter(adapter)
        adapter?.registerAdapterDataObserver(observer)
        invalidateIsEmpty()
    }

}