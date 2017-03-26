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

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.transition.ChangeBounds
import android.transition.TransitionValues
import android.util.AttributeSet
import android.view.ViewGroup

class BackgroundColorChangeBounds : ChangeBounds {

    constructor() : super()

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    companion object {
        const val PROPNAME_COLOR = "BackgroundColorChangeBounds:color"
    }

    override fun getTransitionProperties(): Array<out String> {
        return arrayOf(PROPNAME_COLOR, *super.getTransitionProperties())
    }

    override fun captureStartValues(transitionValues: TransitionValues?) {
        super.captureStartValues(transitionValues)
        transitionValues?.values?.put(PROPNAME_COLOR, (transitionValues.view.background as ColorDrawable).color)
    }

    override fun captureEndValues(transitionValues: TransitionValues?) {
        super.captureEndValues(transitionValues)
        transitionValues?.values?.put(PROPNAME_COLOR, (transitionValues.view.background as ColorDrawable).color)
    }

    override fun createAnimator(sceneRoot: ViewGroup?, startValues: TransitionValues?, endValues: TransitionValues?): Animator {
        val colorAnim = ValueAnimator.ofArgb(startValues?.values?.get(PROPNAME_COLOR) as Int, endValues?.values?.get(PROPNAME_COLOR) as Int)
        colorAnim.addUpdateListener {
            endValues.view?.setBackgroundColor(colorAnim.animatedValue as Int)

        }
        val animatorSet = AnimatorSet()
        animatorSet.playTogether(super.createAnimator(sceneRoot, startValues, endValues), colorAnim)
        return animatorSet
    }

}