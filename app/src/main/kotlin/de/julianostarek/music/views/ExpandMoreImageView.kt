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
import android.support.graphics.drawable.AnimatedVectorDrawableCompat
import android.util.AttributeSet
import android.widget.ImageView
import de.julianostarek.music.R

class ExpandMoreImageView : ImageView {
    var isExpanded = false
        private set
    private var collapseAnim: AnimatedVectorDrawableCompat? = null
    private var expandAnim: AnimatedVectorDrawableCompat? = null

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        init()
    }

    private fun init() {
        isClickable = true
        collapseAnim = AnimatedVectorDrawableCompat.create(context, R.drawable.arrow_anim_collapse)
        expandAnim = AnimatedVectorDrawableCompat.create(context, R.drawable.arrow_anim_expand)
        setImageDrawable(expandAnim)
    }

    fun toggle() {
        val drawable = if (isExpanded) collapseAnim else expandAnim
        setImageDrawable(drawable)
        drawable?.start()
        isExpanded = !isExpanded
    }

}