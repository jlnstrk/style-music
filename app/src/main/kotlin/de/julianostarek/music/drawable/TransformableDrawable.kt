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

package de.julianostarek.music.drawable

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Outline
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.support.annotation.ColorInt

class TransformableDrawable(@ColorInt color: Int, cornerRadius: Float) : Drawable() {
    private val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    var cornerRadius: Float = cornerRadius
        set(value) {
            println(value)
            field = value
            invalidateSelf()
        }

    var color: Int
        get() = paint.color
        set(color) {
            println(color)
            paint.color = color
            invalidateSelf()
        }

    init {
        paint.color = color
    }

    override fun draw(canvas: Canvas) {
        canvas.drawRoundRect(bounds.left.toFloat(), bounds.top.toFloat(), bounds.right.toFloat(), bounds.bottom.toFloat(), cornerRadius, cornerRadius, paint)
    }

    override fun getOutline(outline: Outline) {
        outline.setRoundRect(bounds, cornerRadius)
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
        invalidateSelf()
    }

    override fun setColorFilter(cf: ColorFilter?) {
        paint.colorFilter = cf
        invalidateSelf()
    }

    override fun getOpacity(): Int {
        return paint.alpha
    }

}