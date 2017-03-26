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

package de.julianostarek.music.anko

import android.graphics.Typeface
import android.support.constraint.ConstraintSet
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import de.julianostarek.music.R
import de.julianostarek.music.anko.viewholders.UniversalViewHolder
import org.jetbrains.anko.*

class DetailHeader(itemView: View, ui: UI, activity: AppCompatActivity) : UniversalViewHolder<Nothing>(itemView, ui, activity) {

    class UI(private val isSharedElement: Boolean) : UniversalUI() {
        lateinit var icon: ImageView
            private set

        override fun createView(ui: AnkoContext<ViewGroup>) = with(ui) {
            constraintLayout {
                if (ctx.configuration.portrait && isSharedElement) {
                    transitionName = ctx.getString(R.string.transition_name_background)
                    isTransitionGroup = false
                }
                layoutParams = ViewGroup.LayoutParams(matchParent, wrapContent)
                backgroundResource = R.color.primary_light_background

                icon = imageView {
                    transitionName = ctx.getString(R.string.transition_name_icon)
                    id = 1
                    padding = dip(12)
                }
                title = textView {
                    transitionName = ctx.getString(R.string.transition_name_title)
                    id = 2
                    typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 34F)
                    maxLines = 2
                    ellipsize = TextUtils.TruncateAt.END
                }
                subtitle = textView {
                    transitionName = ctx.getString(R.string.transition_name_subtitle)
                    id = 3
                    typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 20F)
                    maxLines = 1
                    ellipsize = TextUtils.TruncateAt.END
                }

                val constraints = ConstraintSet()

                constraints.constrainHeight(1, dip(48))
                constraints.constrainWidth(1, dip(48))
                constraints.connect(1, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, dip(4))
                constraints.connect(1, ConstraintSet.TOP, 2, ConstraintSet.TOP, 0)

                constraints.constrainHeight(2, wrapContent)
                constraints.connect(2, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, dip(24))
                constraints.connect(2, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, dip(72))
                constraints.connect(2, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, dip(72))
                constraints.connect(2, ConstraintSet.BOTTOM, 3, ConstraintSet.TOP, dip(8))

                constraints.constrainHeight(3, wrapContent)
                //constraints.connect(3, ConstraintSet.TOP, 2, ConstraintSet.BOTTOM, dip(8))
                constraints.connect(3, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, dip(72))
                constraints.connect(3, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, dip(72))
                constraints.connect(3, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, dip(24))

                constraints.applyTo(this)
            }
        }

    }

}