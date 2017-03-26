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

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.support.v4.content.ContextCompat
import android.support.v7.widget.CardView
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.PopupWindow
import android.widget.SeekBar
import android.widget.TextView
import de.julianostarek.music.R
import mobile.substance.sdk.utils.MusicCoreUtil
import org.jetbrains.anko.*
import org.jetbrains.anko.cardview.v7.cardView

class MarkerSeekBar : SeekBar, SeekBar.OnSeekBarChangeListener {
    private var onSeekBarChangeListener: OnSeekBarChangeListener? = null
    private lateinit var popupWindow: PopupWindow
    private lateinit var popupWindowView: CardView
    private lateinit var popupWindowTextView: TextView
    private var animator: Animator? = null
    private var isShowing = false
    private var isShowAnimationFinished = false


    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun setOnSeekBarChangeListener(l: OnSeekBarChangeListener?) {
        this.onSeekBarChangeListener = l
    }

    init {
        super.setOnSeekBarChangeListener(this)
        initPopupWindow()
    }

    private fun initPopupWindow() {
        popupWindow = PopupWindow(context.UI {
            cardView {
                translationY = dip(24).toFloat()
                visibility = View.INVISIBLE
                cardElevation = 0F
                popupWindowView = this
                layoutParams = ViewGroup.LayoutParams(wrapContent, wrapContent)

                popupWindowTextView = textView {
                    padding = dip(8)
                    layoutParams = FrameLayout.LayoutParams(wrapContent, wrapContent).apply {
                        gravity = Gravity.CENTER
                    }
                    setTextAppearance(ctx, R.style.ItemTitleTextAppearance)
                    textColor = ContextCompat.getColor(ctx, R.color.primary_text_color_light)
                }

            }
        }.view, wrapContent, wrapContent)
        popupWindow.apply {
            elevation = dip(4).toFloat()
            animationStyle = 0
            isClippingEnabled = false
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
        popupWindowView.addOnLayoutChangeListener(object : OnLayoutChangeListener {
            override fun onLayoutChange(v: View?, left: Int, top: Int, right: Int, bottom: Int, oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int) {
                popupWindowView.removeOnLayoutChangeListener(this)
                isShowAnimationFinished = false
                val animator = ViewAnimationUtils.createCircularReveal(popupWindowView, popupWindowView.width / 2, popupWindowView.height / 2, 0F, popupWindowView.width / 2F)
                animator.duration = 250
                animator.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        isShowAnimationFinished = true
                    }
                })
                popupWindowView.visibility = View.VISIBLE
                popupWindowView.animate().translationY(0F).setDuration(250).start()
                animator.start()
            }
        })
        popupWindow.showAsDropDown(this@MarkerSeekBar, thumb.bounds.centerX() - popupWindowTextView.width / 4, -dip(56))
        isShowing = true
        onSeekBarChangeListener?.onStartTrackingTouch(seekBar)
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
        if (popupWindow.isShowing) {
            if ((animator?.isRunning ?: false) || (isShowing && !isShowAnimationFinished)) {
                isShowAnimationFinished = false
                animator?.cancel()
                popupWindowView.visibility = View.INVISIBLE
                popupWindow.dismiss()
                onSeekBarChangeListener?.onStopTrackingTouch(seekBar)
                return
            } else isShowAnimationFinished = false
            animator = ViewAnimationUtils.createCircularReveal(popupWindowView, popupWindowView.width / 2, popupWindowView.height / 2, popupWindowView.width / 2F, 0F)
            animator?.duration = 250
            animator?.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    popupWindowView.visibility = View.INVISIBLE
                    popupWindow.dismiss()
                    isShowing = false
                }
            })
            popupWindowView.animate().translationY(dip(24).toFloat()).setDuration(250).start()
            animator?.start()
        }
        onSeekBarChangeListener?.onStopTrackingTouch(seekBar)
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        if (fromUser) {
            popupWindow.update(this, thumb.bounds.centerX() - popupWindowTextView.width / 4, -dip(56), wrapContent, wrapContent)
            popupWindowTextView.text = MusicCoreUtil.stringForTime(progress.toLong())
        }
        onSeekBarChangeListener?.onProgressChanged(seekBar, progress, fromUser)
    }

}