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

import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.widget.ImageView

fun Drawable.tinted(color: Int): Drawable? {
    val newDrawable = DrawableCompat.wrap(mutate())
    DrawableCompat.setTintMode(newDrawable, PorterDuff.Mode.SRC_IN)
    DrawableCompat.setTint(newDrawable, color)
    return newDrawable
}

fun ImageView.setImageResourceTinted(drawableResId: Int, color: Int) {
    setImageDrawable(ContextCompat.getDrawable(context, drawableResId).tinted(color))
}

fun Int.isLight(): Boolean {
    if (this == Color.BLACK)
        return false
    else if (this == Color.WHITE || this == Color.TRANSPARENT) return true
    val darkness = 1 - (0.299 * Color.red(this) + 0.587 * Color.green(this) + 0.114 * Color.blue(this)) / 255
    return darkness < 0.4
}