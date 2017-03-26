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
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import de.julianostarek.music.extensions.bind
import org.jetbrains.anko.AnkoComponent

open class UniversalViewHolder<T>(view: View, val ui: UniversalUI, protected val activity: AppCompatActivity) : RecyclerView.ViewHolder(view), View.OnClickListener {
    var item: T? = null
        set(value) {
            field = value
            bind(value!!)
            invalidate()
        }

    init {
        itemView.setOnClickListener(this)
    }

    open fun invalidate() {

    }

    override fun onClick(v: View?) {

    }

    abstract class UniversalUI : AnkoComponent<ViewGroup> {
        var title: TextView? = null
        var subtitle: TextView? = null
        var image: ImageView? = null
    }

}