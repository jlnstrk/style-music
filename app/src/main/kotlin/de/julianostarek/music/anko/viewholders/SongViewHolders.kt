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
import android.support.constraint.ConstraintLayout
import android.support.constraint.ConstraintSet
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import co.metalab.asyncawait.async
import de.julianostarek.music.R
import de.julianostarek.music.anko.constraintLayout
import de.julianostarek.music.anko.horizontalSquareImageView
import de.julianostarek.music.anko.verticalSquareImageView
import de.julianostarek.music.extensions.getFavorites
import de.julianostarek.music.helper.PopupHelper
import mobile.substance.sdk.music.core.dataLinkers.MusicData
import mobile.substance.sdk.music.core.objects.Song
import mobile.substance.sdk.music.loading.MusicType
import mobile.substance.sdk.music.playback.PlaybackRemote
import mobile.substance.sdk.utils.MusicCoreUtil
import org.jetbrains.anko.*

sealed class SongViewHolders {

    class Normal(itemView: View, ui: UI, activity: AppCompatActivity) : UniversalViewHolder<Song>(itemView, ui, activity) {

        init {
            ui.menu.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            when (v) {
                itemView -> PlaybackRemote.play(MusicData.getSongs(), MusicData.getSongs().indexOf(item))
                (ui as UI).menu -> PopupHelper.showPopup(v, item!!, activity, true)
            }
        }

        class UI : UniversalUI() {
            lateinit var duration: TextView
                private set
            lateinit var menu: ImageView
                private set

            override fun createView(ui: AnkoContext<ViewGroup>) = with(ui) {
                constraintLayout {
                    layoutParams = ViewGroup.LayoutParams(matchParent, dip(72))
                    val ta = ctx.obtainStyledAttributes(arrayOf(android.R.attr.selectableItemBackground, android.R.attr.selectableItemBackgroundBorderless).toIntArray())
                    background = ta.getDrawable(0)

                    image = imageView {
                        id = 1
                        adjustViewBounds = true
                        layoutParams = ConstraintLayout.LayoutParams(dip(40), dip(40))
                        (layoutParams as ConstraintLayout.LayoutParams).marginStart = dip(16)
                    }
                    title = textView {
                        id = 2
                        bottomPadding = dip(20)
                        setTextAppearance(context, R.style.ItemTitleTextAppearance)
                        maxLines = 1
                        ellipsize = TextUtils.TruncateAt.END
                    }
                    subtitle = textView {
                        id = 3
                        topPadding = dip(20)
                        setTextAppearance(context, R.style.ItemSubtitleTextAppearance)
                        maxLines = 1
                        ellipsize = TextUtils.TruncateAt.END
                    }
                    menu = imageView {
                        id = 4
                        isClickable = true
                        isLongClickable = false
                        padding = dip(8)
                        imageResource = R.drawable.ic_more_vert_black_transparent_24dp
                        background = ta.getDrawable(1)
                    }
                    duration = textView {
                        id = 5
                    }
                    ta.recycle()

                    val constraints = ConstraintSet()
                    constraints.centerVertically(1, ConstraintSet.PARENT_ID)
                    constraints.constrainHeight(1, dip(40))
                    constraints.constrainWidth(1, dip(40))
                    constraints.connect(1, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, dip(16))

                    constraints.centerVertically(2, ConstraintSet.PARENT_ID)
                    constraints.constrainHeight(2, wrapContent)
                    constraints.connect(2, ConstraintSet.START, 1, ConstraintSet.END, dip(16))
                    constraints.connect(2, ConstraintSet.END, 5, ConstraintSet.START, dip(8))

                    constraints.centerVertically(3, ConstraintSet.PARENT_ID)
                    constraints.constrainHeight(3, wrapContent)
                    constraints.connect(3, ConstraintSet.START, 1, ConstraintSet.END, dip(16))
                    constraints.connect(3, ConstraintSet.END, 5, ConstraintSet.START, dip(8))

                    constraints.centerVertically(4, ConstraintSet.PARENT_ID)
                    constraints.constrainHeight(4, dip(40))
                    constraints.constrainWidth(4, dip(40))
                    constraints.connect(4, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, dip(8))

                    constraints.centerVertically(5, ConstraintSet.PARENT_ID)
                    constraints.constrainHeight(5, wrapContent)
                    constraints.connect(5, ConstraintSet.END, 4, ConstraintSet.START, dip(16))

                    constraints.applyTo(this)
                }
            }

        }

    }

    open class Indexed(itemView: View, ui: UI, activity: AppCompatActivity) : UniversalViewHolder<Song>(itemView, ui, activity) {

        override fun invalidate() {
            (ui as SongViewHolders.Indexed.UI).index.text = adapterPosition.toString()
        }

        open class UI : UniversalUI() {
            lateinit var duration: TextView
                protected set
            lateinit var index: TextView
                protected set

            override fun createView(ui: AnkoContext<ViewGroup>) = with(ui) {
                constraintLayout {
                    layoutParams = ViewGroup.LayoutParams(matchParent, dip(56))
                    val ta = ctx.obtainStyledAttributes(arrayOf(android.R.attr.selectableItemBackground).toIntArray())
                    background = ta.getDrawable(0)
                    ta.recycle()
                    isClickable = true

                    index = textView {
                        id = 1
                        setTextAppearance(context, R.style.ItemTitleTextAppearance)
                        textColor = ContextCompat.getColor(ctx, R.color.secondary_text_color_light)
                    }
                    title = textView {
                        id = 2
                        setTextAppearance(context, R.style.ItemTitleTextAppearance)
                        maxLines = 1
                        ellipsize = TextUtils.TruncateAt.END
                    }
                    duration = textView {
                        id = 3
                        setTextAppearance(context, R.style.ItemSubtitleTextAppearance)
                    }

                    val constraints = ConstraintSet()

                    constraints.centerVertically(1, ConstraintSet.PARENT_ID)
                    constraints.constrainHeight(1, wrapContent)
                    constraints.constrainWidth(1, wrapContent)
                    constraints.connect(1, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, dip(72))

                    constraints.centerVertically(2, ConstraintSet.PARENT_ID)
                    constraints.constrainHeight(2, wrapContent)
                    constraints.connect(2, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, dip(112))
                    constraints.connect(2, ConstraintSet.END, 3, ConstraintSet.START, dip(16))

                    constraints.centerVertically(3, ConstraintSet.PARENT_ID)
                    constraints.constrainHeight(3, wrapContent)
                    constraints.constrainWidth(3, wrapContent)
                    constraints.connect(3, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, dip(16))

                    constraints.applyTo(this)
                }
            }

        }

    }

    class Favorite(itemView: View, ui: UI, activity: AppCompatActivity) : ColorableViewHolder<Song>(itemView, ui, activity) {

        override fun onClick(v: View?) {
            async {
                val favorites = await { getFavorites<Song>(activity, MusicType.SONGS) }
                PlaybackRemote.play(favorites, favorites.indexOf(item))
            }
        }

        class UI : ColorableUI() {

            override fun createView(ui: AnkoContext<ViewGroup>) = with(ui) {
                frameLayout {
                    colorable = this
                    layoutParams = ViewGroup.LayoutParams(matchParent, dip(68))
                    isClickable = true
                    val ta = ctx.obtainStyledAttributes(arrayOf(android.R.attr.selectableItemBackground).toIntArray())
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) foreground = ta.getDrawable(0)
                    ta.recycle()

                    image = horizontalSquareImageView().lparams(wrapContent, matchParent)
                    title = textView {
                        leftPadding = dip(88)
                        rightPadding = dip(16)
                        bottomPadding = dip(20)
                        setTextAppearance(context, R.style.ItemTitleTextAppearance)
                        maxLines = 1
                        ellipsize = TextUtils.TruncateAt.END
                    }.lparams(matchParent, wrapContent) {
                        gravity = Gravity.CENTER_VERTICAL
                    }
                    subtitle = textView {
                        leftPadding = dip(88)
                        rightPadding = dip(16)
                        topPadding = dip(20)
                        setTextAppearance(context, R.style.ItemSubtitleTextAppearance)
                        maxLines = 1
                        ellipsize = TextUtils.TruncateAt.END
                    }.lparams(matchParent, wrapContent) {
                        gravity = Gravity.CENTER_VERTICAL
                    }
                }
            }

        }

    }

}