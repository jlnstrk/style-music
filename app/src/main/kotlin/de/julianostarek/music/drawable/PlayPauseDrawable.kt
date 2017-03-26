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

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.*
import android.graphics.Paint.Style
import android.graphics.drawable.Drawable
import android.util.Property
import android.view.animation.DecelerateInterpolator
import de.julianostarek.music.R

class PlayPauseDrawable(context: Context, iconColor: Int) : Drawable() {
    private val leftPauseBar: Path
    private val paint: Paint
    private val pauseBarDistance: Float
    private val pauseBarHeight: Float
    private val pauseBarWidth: Float
    private val rightPauseBar: Path
    private var animator: Animator? = null
    private var height: Float = 0.toFloat()
    private var isPlay: Boolean = false
    private var state: Int = 0
    private var isPlaySet: Boolean = false
    private var progress: Float = 0.toFloat()
        set(f) {
            field = f
            invalidateSelf()
        }
    private var width: Float = 0F

    init {
        leftPauseBar = Path()
        rightPauseBar = Path()
        paint = Paint()
        state = STATE_PAUSE
        val resources = context.resources
        paint.isAntiAlias = true
        paint.style = Style.FILL
        paint.color = iconColor
        pauseBarWidth = resources.getDimensionPixelSize(R.dimen.pause_bar_width).toFloat()
        pauseBarHeight = resources.getDimensionPixelSize(R.dimen.pause_bar_height).toFloat()
        pauseBarDistance = resources.getDimensionPixelSize(R.dimen.pause_bar_distance).toFloat()
    }

    private val pausePlayAnimator: Animator
        get() {
            var f = 0.0f
            isPlaySet = !isPlaySet
            val property = PROGRESS
            val fArr = FloatArray(2)
            fArr[0] = if (isPlay) 1.0f else 0.0f
            if (!isPlay) {
                f = 1.0f
            }
            fArr[1] = f
            val ofFloat = ObjectAnimator.ofFloat(this, property, *fArr)
            ofFloat.addListener(ListenerAdapter())
            return ofFloat
        }

    override fun draw(canvas: Canvas) {
        var f = 0.0f
        leftPauseBar.rewind()
        rightPauseBar.rewind()
        val lerp = lerp(pauseBarDistance, 0.0f, progress)
        var lerp2 = lerp(pauseBarWidth, pauseBarHeight / 1.75f, progress)
        if (progress == 1.0f) {
            lerp2 = Math.round(lerp2).toFloat()
        }
        var lerp3 = lerp(0.0f, lerp2, progress)
        val lerp4 = lerp(2.0f * lerp2 + lerp, lerp2 + lerp, progress)
        leftPauseBar.moveTo(0.0f, 0.0f)
        leftPauseBar.lineTo(lerp3, -pauseBarHeight)
        leftPauseBar.lineTo(lerp2, -pauseBarHeight)
        leftPauseBar.lineTo(lerp2, 0.0f)
        leftPauseBar.close()
        rightPauseBar.moveTo(lerp2 + lerp, 0.0f)
        rightPauseBar.lineTo(lerp2 + lerp, -pauseBarHeight)
        rightPauseBar.lineTo(lerp4, -pauseBarHeight)
        rightPauseBar.lineTo(2.0f * lerp2 + lerp, 0.0f)
        rightPauseBar.close()
        canvas.save()
        canvas.translate(lerp(0.0f, pauseBarHeight / 8.0f, progress), 0.0f)
        lerp3 = if (isPlay) 1.0f - progress else progress
        if (isPlay) {
            f = 90.0f
        }
        canvas.rotate(lerp(f, 90.0f + f, lerp3), width / 2.0f, height / 2.0f)
        canvas.translate(width / 2.0f - (lerp2 * 2.0f + lerp) / 2.0f, height / 2.0f + pauseBarHeight / 2.0f)
        canvas.drawPath(leftPauseBar, paint)
        canvas.drawPath(rightPauseBar, paint)
        canvas.restore()
    }

    override fun getOpacity(): Int {
        return -3
    }

    override fun onBoundsChange(rect: Rect) {
        super.onBoundsChange(rect)
        if (rect.width() > 0 && rect.height() > 0) {
            width = rect.width().toFloat()
            height = rect.height().toFloat()
        }
    }

    override fun setAlpha(i: Int) {
        paint.alpha = i
        invalidateSelf()
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
        invalidateSelf()
    }

    fun setPause(animate: Boolean) {
        if (!animate) {
            isPlaySet = false
            isPlay = false
            progress = 0.0f
        } else if (isPlaySet) toggle()
    }

    fun setPlay(animate: Boolean) {
        if (!animate) {
            isPlaySet = true
            isPlay = true
            progress = 1.0f
        } else if (!isPlaySet) toggle()
    }

    fun toggle() {
        if (animator != null) animator!!.cancel()
        animator = pausePlayAnimator
        animator!!.interpolator = DecelerateInterpolator()
        animator!!.duration = PLAY_PAUSE_ANIMATION_DURATION
        animator!!.start()
        toggleState()
    }

    private fun toggleState() {
        when (state) {
            STATE_PAUSE -> state = STATE_PLAY
            STATE_PLAY -> state = STATE_PAUSE
        }
    }

    var playPauseState: Int
        get() = state
        set(newState) {
            when (newState) {
                STATE_PAUSE -> {
                    setPause(true)
                    state = STATE_PAUSE
                }
                STATE_PLAY -> {
                    setPlay(true)
                    state = STATE_PLAY
                }
            }
        }

    internal inner class ListenerAdapter : AnimatorListenerAdapter() {

        override fun onAnimationEnd(animator: Animator) {
            this@PlayPauseDrawable.isPlay = !this@PlayPauseDrawable.isPlay
        }
    }

    companion object {
        const val STATE_PLAY = 1
        const val STATE_PAUSE = 2
        const val PLAY_PAUSE_ANIMATION_DURATION: Long = 200
        private val PROGRESS: Property<PlayPauseDrawable, Float> = object : Property<PlayPauseDrawable, Float>(Float::class.java, "progress") {
            override fun get(d: PlayPauseDrawable): Float {
                return d.progress
            }

            override fun set(d: PlayPauseDrawable, value: Float?) {
                d.progress = value!!
            }
        }

        private fun lerp(f: Float, f2: Float, f3: Float): Float {
            return (f2 - f) * f3 + f
        }
    }
}




