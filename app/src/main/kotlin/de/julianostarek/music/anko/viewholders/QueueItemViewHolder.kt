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
import android.widget.ImageView
import de.julianostarek.music.R
import de.julianostarek.music.anko.constraintLayout
import de.julianostarek.music.helper.PopupHelper
import mobile.substance.sdk.music.playback.PlaybackRemote
import mobile.substance.sdk.utils.MusicCoreUtil
import org.jetbrains.anko.*

class QueueItemViewHolder(itemView: View, ui: UI, activity: AppCompatActivity) : SongViewHolders.Indexed(itemView, ui, activity) {

    init {
        ui.menu.setOnClickListener(this)
    }

    override fun invalidate() {
        // super.invalidate()
        // Prevent default index override
    }

    override fun onClick(v: View?) {
        when (v) {
            itemView -> PlaybackRemote.play(PlaybackRemote.getQueue(), PlaybackRemote.getQueue().indexOf(item))
            (ui as QueueItemViewHolder.UI).menu -> PopupHelper.showPopup(v, item!!, activity)
        }
    }

    class UI : SongViewHolders.Indexed.UI() {
        lateinit var menu: ImageView
            private set

        override fun createView(ui: AnkoContext<ViewGroup>) = with(ui) {
            constraintLayout {
                layoutParams = ViewGroup.LayoutParams(matchParent, dip(72))
                val ta = ctx.obtainStyledAttributes(arrayOf(android.R.attr.selectableItemBackground, android.R.attr.selectableItemBackgroundBorderless).toIntArray())
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) foreground = ta.getDrawable(0)
                isClickable = true

                image = imageView {
                    id = 1
                    background = ta.getDrawable(1)
                    padding = dip(8)
                    imageResource = R.drawable.ic_reorder_black_24dp
                }
                index = textView {
                    id = 2
                    setTextAppearance(context, R.style.ItemTitleTextAppearance)
                    textColor = ContextCompat.getColor(ctx, R.color.secondary_text_color_light)
                }
                title = textView {
                    id = 3
                    bottomPadding = dip(20)
                    setTextAppearance(context, R.style.ItemTitleTextAppearance)
                    maxLines = 1
                    ellipsize = TextUtils.TruncateAt.END
                }
                subtitle = textView {
                    id = 4
                    topPadding = dip(20)
                    setTextAppearance(context, R.style.ItemSubtitleTextAppearance)
                    maxLines = 1
                    ellipsize = TextUtils.TruncateAt.END
                }
                menu = imageView {
                    id = 5
                    isClickable = true
                    background = ta.getDrawable(1)
                    imageResource = R.drawable.ic_more_vert_black_transparent_24dp
                    padding = dip(8)
                }
                ta.recycle()

                val constraints = ConstraintSet()

                constraints.constrainHeight(1, dip(40))
                constraints.constrainWidth(1, dip(40))
                constraints.centerVertically(1, ConstraintSet.PARENT_ID)
                constraints.connect(1, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, dip(8))

                constraints.constrainWidth(2, wrapContent)
                constraints.constrainHeight(2, wrapContent)
                constraints.centerVertically(2, ConstraintSet.PARENT_ID)
                constraints.connect(2, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, dip(72))

                constraints.constrainHeight(3, wrapContent)
                constraints.centerVertically(3, ConstraintSet.PARENT_ID)
                constraints.connect(3, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, dip(112))
                constraints.connect(3, ConstraintSet.END, 5, ConstraintSet.START, dip(16))

                constraints.constrainHeight(4, wrapContent)
                constraints.centerVertically(4, ConstraintSet.PARENT_ID)
                constraints.connect(4, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, dip(112))
                constraints.connect(4, ConstraintSet.END, 5, ConstraintSet.START, dip(16))

                constraints.constrainHeight(5, dip(40))
                constraints.constrainWidth(5, dip(40))
                constraints.centerVertically(5, ConstraintSet.PARENT_ID)
                constraints.connect(5, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, dip(8))

                constraints.applyTo(this)

            }
        }

    }

}