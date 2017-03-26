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
import android.animation.ObjectAnimator
import android.content.Context
import android.transition.ChangeBounds
import android.transition.TransitionValues
import android.util.AttributeSet
import android.view.ViewGroup
import org.jetbrains.anko.dip

class GenericDetailContainerTransition : ChangeBounds {

    constructor() : super()

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    override fun createAnimator(sceneRoot: ViewGroup, startValues: TransitionValues?, endValues: TransitionValues?): Animator? {
        val changeBounds = super.createAnimator(sceneRoot, startValues, endValues)

        val closeIcon = (startValues?.view?.findViewById(1) ?: endValues?.view?.findViewById(1))
        val minusFiftySixDip = closeIcon?.dip(-56)?.toFloat()
        val animatorSet = AnimatorSet()
        val finalTranslationX = if (closeIcon!!.translationX == 0F) minusFiftySixDip!! else 0F
        val animator = ObjectAnimator.ofFloat(closeIcon, "translationX", closeIcon.translationX, finalTranslationX)
        animatorSet.playTogether(changeBounds, animator)
        return animatorSet
    }

}