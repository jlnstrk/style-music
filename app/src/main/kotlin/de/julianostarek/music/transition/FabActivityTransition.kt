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

package de.julianostarek.music.transition

import android.animation.*
import android.content.Context
import android.graphics.Color
import android.support.design.widget.CoordinatorLayout
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.transition.ChangeBounds
import android.transition.TransitionValues
import android.util.AttributeSet
import android.view.ViewGroup
import de.julianostarek.music.drawable.TransformableDrawable
import de.julianostarek.music.helper.AppColors
import org.jetbrains.anko.dip

class FabActivityTransition : ChangeBounds {
    private val PROPNAME_COLOR = "TransformableDrawable:color"
    private val PROPNAME_CORNER_RADIUS = "TransformableDrawable:cornerRadius"
    private val TRANSITION_PROPERTIES = arrayOf(PROPNAME_COLOR, PROPNAME_CORNER_RADIUS)

    constructor() : super()

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    override fun getTransitionProperties(): Array<String> {
        return arrayOf(*TRANSITION_PROPERTIES, *super.getTransitionProperties())
    }

    override fun captureStartValues(transitionValues: TransitionValues) {
        super.captureStartValues(transitionValues)
        val view = transitionValues.view

        val isReturn = isReturn(transitionValues)
        transitionValues.values.put(PROPNAME_COLOR, if (isReturn) Color.WHITE else AppColors.ACCENT_COLOR)
        transitionValues.values.put(PROPNAME_CORNER_RADIUS, if (isReturn) 0F else view.dip(28).toFloat())
    }

    override fun captureEndValues(transitionValues: TransitionValues) {
        super.captureEndValues(transitionValues)
        val view = transitionValues.view

        val isReturn = !isReturn(transitionValues)
        transitionValues.values.put(PROPNAME_COLOR, if (isReturn) AppColors.ACCENT_COLOR else Color.WHITE)
        transitionValues.values.put(PROPNAME_CORNER_RADIUS, if (isReturn) view.dip(28).toFloat() else 0F)
    }

    override fun createAnimator(sceneRoot: ViewGroup, startValues: TransitionValues?, endValues: TransitionValues?): Animator? {
        val base = super.createAnimator(sceneRoot, startValues, endValues)

        val background = TransformableDrawable(AppColors.ACCENT_COLOR, startValues!!.values[PROPNAME_CORNER_RADIUS] as Float)
        endValues!!.view.background = background

        val colorAnimator = ValueAnimator.ofArgb(startValues.values[PROPNAME_COLOR] as Int, endValues.values[PROPNAME_COLOR] as Int)
        val cornerRadiusAnimator = ValueAnimator.ofFloat(startValues.values[PROPNAME_CORNER_RADIUS] as Float, endValues.values[PROPNAME_CORNER_RADIUS] as Float)

        val updateListener = ValueAnimator.AnimatorUpdateListener {
            when (it) {
                colorAnimator -> background.color = it.animatedValue as Int
                cornerRadiusAnimator -> background.cornerRadius = it.animatedValue as Float
            }
        }

        colorAnimator.addUpdateListener(updateListener)
        cornerRadiusAnimator.addUpdateListener(updateListener)

        if (!isReturn(startValues)) {
            val vg = (endValues.view as ViewGroup).getChildAt(1) as ViewGroup
            var offset = vg.dip(32).toFloat()
            for (i in 0..vg.childCount) {
                val v = if (i == vg.childCount) (vg.parent as CoordinatorLayout).getChildAt(0) else vg.getChildAt(i)
                v.translationY = offset
                v.alpha = 0F
                v.animate()
                        .alpha(1F)
                        .translationY(0F)
                        .setDuration(400)
                        .setStartDelay(200)
                        .setInterpolator(FastOutSlowInInterpolator())
                        .start()
                offset *= 1.8F
            }
        } else {
            val vg = startValues.view as ViewGroup
            (0..vg.childCount - 1)
                    .map { vg.getChildAt(it) }
                    .forEach { it.alpha = 0F }
        }

        val transition = AnimatorSet()
        transition.playTogether(cornerRadiusAnimator, colorAnimator, base)
        return transition
    }

    private fun isReturn(transitionValues: TransitionValues): Boolean = transitionValues.view.width > transitionValues.view.dip(56)

}