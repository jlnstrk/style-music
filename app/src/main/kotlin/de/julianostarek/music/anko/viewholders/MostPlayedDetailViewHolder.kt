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

import android.os.Build
import android.support.constraint.ConstraintSet
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import de.julianostarek.music.R
import de.julianostarek.music.anko.constraintLayout
import mobile.substance.sdk.music.core.objects.MediaObject
import mobile.substance.sdk.utils.MusicCoreUtil
import org.jetbrains.anko.*

class MostPlayedDetailViewHolder<T : MediaObject>(view: View, ui: UI, activity: AppCompatActivity) : UniversalViewHolder<T>(view, ui, activity) {

    class UI : UniversalUI() {
        lateinit var attribute: TextView
            private set
        lateinit var index: TextView
            private set

        override fun createView(ui: AnkoContext<ViewGroup>) = with(ui) {
            constraintLayout {
                layoutParams = ViewGroup.LayoutParams(matchParent, dip(88))
                val ta = ctx.obtainStyledAttributes(arrayOf(android.R.attr.selectableItemBackground, android.R.attr.selectableItemBackgroundBorderless).toIntArray())
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) foreground = ta.getDrawable(0)
                isClickable = true

                index = textView {
                    id = 1
                    setTextAppearance(context, R.style.ItemTitleTextAppearance)
                    textColor = ContextCompat.getColor(ctx, R.color.secondary_text_color_light)
                }
                image = imageView {
                    id = 2
                    background = ta.getDrawable(1)
                    padding = dip(8)
                    imageResource = R.drawable.ic_reorder_black_24dp
                }
                title = textView {
                    id = 3
                    setTextAppearance(context, R.style.ItemTitleTextAppearance)
                    maxLines = 1
                    ellipsize = TextUtils.TruncateAt.END
                }
                subtitle = textView {
                    id = 4
                    setTextAppearance(context, R.style.ItemSubtitleTextAppearance)
                    maxLines = 1
                    ellipsize = TextUtils.TruncateAt.END
                }
                attribute = textView {
                    id = 5
                    setTextAppearance(context, R.style.ItemSubtitleTextAppearance)
                    maxLines = 1
                    ellipsize = TextUtils.TruncateAt.END
                }
                ta.recycle()

                val constraints = ConstraintSet()

                constraints.constrainHeight(2, dip(40))
                constraints.constrainWidth(2, dip(40))
                constraints.centerVertically(2, ConstraintSet.PARENT_ID)
                constraints.connect(2, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.END, dip(72))

                constraints.constrainWidth(1, wrapContent)
                constraints.constrainHeight(1, wrapContent)
                constraints.centerVertically(1, ConstraintSet.PARENT_ID)
                constraints.connect(1, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, dip(16))

                constraints.constrainWidth(3, matchParent)
                constraints.constrainHeight(3, wrapContent)
                constraints.connect(3, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, dip(112))
                constraints.connect(3, ConstraintSet.END, 5, ConstraintSet.START, dip(16))
                constraints.connect(3, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, dip(16))

                constraints.constrainWidth(4, matchParent)
                constraints.constrainHeight(4, wrapContent)
                constraints.centerVertically(4, ConstraintSet.PARENT_ID)
                constraints.connect(4, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, dip(112))
                constraints.connect(4, ConstraintSet.END, 5, ConstraintSet.START, dip(16))

                constraints.constrainHeight(5, matchParent)
                constraints.constrainWidth(5, wrapContent)
                constraints.connect(5, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, dip(8))
                constraints.connect(5, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, dip(16))

                constraints.applyTo(this)

            }
        }

    }

}