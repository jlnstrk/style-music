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

import android.content.Intent
import android.os.Build
import android.support.constraint.ConstraintSet
import android.support.v4.app.ActivityCompat
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.view.GravityCompat
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import de.julianostarek.music.R
import de.julianostarek.music.activities.AlbumActivity
import de.julianostarek.music.activities.ArtistActivity
import de.julianostarek.music.activities.PlaylistActivity
import de.julianostarek.music.anko.constraintLayout
import de.julianostarek.music.anko.horizontalSquareImageView
import de.julianostarek.music.helper.AppColors
import mobile.substance.sdk.colors.ColorConstants
import mobile.substance.sdk.music.core.objects.*
import mobile.substance.sdk.music.playback.PlaybackRemote
import mobile.substance.sdk.utils.MusicCoreUtil
import org.jetbrains.anko.*

sealed class MediaItemViewHolders {

    class Big<T : MediaObject>(itemView: View, ui: UI, activity: AppCompatActivity) : ColorableViewHolder<T>(itemView, ui, activity) {

        override fun onClick(v: View?) {
            when (item) {
                is Album -> ActivityCompat.startActivity(activity, Intent(activity, AlbumActivity::class.java).putExtra("album_id", item!!.id), ActivityOptionsCompat.makeSceneTransitionAnimation(activity).toBundle())
                is Artist -> ActivityCompat.startActivity(activity, Intent(activity, ArtistActivity::class.java).putExtra("artist_id", item!!.id), ActivityOptionsCompat.makeSceneTransitionAnimation(activity).toBundle())
                is Playlist -> ActivityCompat.startActivity(activity, Intent(activity, PlaylistActivity::class.java).putExtra("playlist_id", item!!.id), ActivityOptionsCompat.makeSceneTransitionAnimation(activity).toBundle())
                is Song -> PlaybackRemote.play(item as Song)
            }
        }

        open class UI : ColorableUI() {
            lateinit var icon: ImageView
                protected set

            override fun createView(ui: AnkoContext<ViewGroup>) = with(ui) {
                constraintLayout {
                    val ta = ctx.obtainStyledAttributes(arrayOf(android.R.attr.selectableItemBackground).toIntArray())
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) foreground = ta.getDrawable(0)
                    ta.recycle()
                    isClickable = true

                    image = horizontalSquareImageView {
                        id = 1
                        adjustViewBounds = true
                        scaleType = ImageView.ScaleType.CENTER_CROP
                    }
                    colorable = frameLayout {
                        id = 2

                        icon = imageView {
                            padding = dip(8)
                        }.lparams(dip(40), dip(40)) {
                            gravity = Gravity.CENTER_VERTICAL
                            leftMargin = dip(8)
                        }
                        title = textView {
                            rightPadding = dip(16)
                            leftPadding = dip(72)
                            bottomPadding = dip(20)

                            setTextAppearance(context, R.style.ItemTitleTextAppearance)
                            maxLines = 1
                            ellipsize = TextUtils.TruncateAt.END
                        }.lparams(matchParent, wrapContent) {
                            gravity = Gravity.CENTER_VERTICAL
                        }
                        subtitle = textView {
                            rightPadding = dip(16)
                            leftPadding = dip(72)
                            topPadding = dip(20)

                            setTextAppearance(context, R.style.ItemSubtitleTextAppearance)
                            maxLines = 1
                            ellipsize = TextUtils.TruncateAt.END
                        }.lparams(matchParent, wrapContent) {
                            gravity = Gravity.CENTER_VERTICAL
                        }
                    }

                    val constraints = ConstraintSet()

                    constraints.constrainWidth(1, wrapContent)
                    constraints.connect(1, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 0)
                    constraints.connect(1, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 0)
                    constraints.connect(1, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, 0)
                    constraints.connect(1, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, 0)

                    constraints.constrainHeight(2, dip(56))
                    constraints.connect(2, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 0)
                    constraints.connect(2, ConstraintSet.END, 1, ConstraintSet.END, 0)
                    constraints.connect(2, ConstraintSet.START, 1, ConstraintSet.START, 0)

                    constraints.applyTo(this)

                }
            }

        }

        class SingleLine(itemView: View, ui: UI, activity: AppCompatActivity) : ColorableViewHolder<Artist>(itemView, ui, activity) {

            override fun onClick(v: View?) {
                ActivityCompat.startActivity(activity, Intent(activity, ArtistActivity::class.java).putExtra("artist_id", item!!.id), ActivityOptionsCompat.makeSceneTransitionAnimation(activity).toBundle())
            }

            open class UI : Big.UI() {

                override fun createView(ui: AnkoContext<ViewGroup>) = with(ui) {
                    constraintLayout {
                        val ta = ctx.obtainStyledAttributes(arrayOf(android.R.attr.selectableItemBackground).toIntArray())
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) foreground = ta.getDrawable(0)
                        ta.recycle()
                        isClickable = true

                        image = horizontalSquareImageView {
                            id = 1
                            adjustViewBounds = true
                            scaleType = ImageView.ScaleType.CENTER_CROP
                        }
                        colorable = frameLayout {
                            id = 2

                            icon = imageView {
                                padding = dip(8)
                            }.lparams(dip(40), dip(40)) {
                                gravity = Gravity.CENTER_VERTICAL
                                leftMargin = dip(8)
                            }
                            title = textView {
                                rightPadding = dip(16)
                                leftPadding = dip(72)

                                setTextAppearance(context, R.style.ItemTitleTextAppearance)
                                maxLines = 1
                                ellipsize = TextUtils.TruncateAt.END
                            }.lparams(matchParent, wrapContent) {
                                gravity = Gravity.CENTER_VERTICAL
                            }
                        }

                        val constraints = ConstraintSet()

                        constraints.constrainWidth(1, wrapContent)
                        constraints.connect(1, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 0)
                        constraints.connect(1, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 0)
                        constraints.connect(1, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, 0)
                        constraints.connect(1, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, 0)

                        constraints.constrainHeight(2, dip(56))
                        constraints.connect(2, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 0)
                        constraints.connect(2, ConstraintSet.END, 1, ConstraintSet.END, 0)
                        constraints.connect(2, ConstraintSet.START, 1, ConstraintSet.START, 0)

                        constraints.applyTo(this)

                    }
                }

            }

        }

    }

    class Small(itemView: View, ui: UI, activity: AppCompatActivity) : UniversalViewHolder<MediaObject>(itemView, ui, activity) {

        override fun onClick(v: View?) {
            when (item) {
                is Album -> ActivityCompat.startActivity(activity, Intent(activity, AlbumActivity::class.java).putExtra("album_id", item!!.id), ActivityOptionsCompat.makeSceneTransitionAnimation(activity).toBundle())
                is Artist -> ActivityCompat.startActivity(activity, Intent(activity, ArtistActivity::class.java).putExtra("artist_id", item!!.id), ActivityOptionsCompat.makeSceneTransitionAnimation(activity).toBundle())
                is Playlist -> ActivityCompat.startActivity(activity, Intent(activity, PlaylistActivity::class.java).putExtra("playlist_id", item!!.id), ActivityOptionsCompat.makeSceneTransitionAnimation(activity).toBundle())
                is Song -> PlaybackRemote.play(item as Song)
            }
        }

        class UI : UniversalUI() {
            lateinit var icon: ImageView
                private set

            override fun createView(ui: AnkoContext<ViewGroup>) = with(ui) {
                frameLayout {
                    layoutParams = ViewGroup.LayoutParams(wrapContent, matchParent)
                    isClickable = true
                    val ta = ctx.obtainStyledAttributes(arrayOf(android.R.attr.selectableItemBackground).toIntArray())
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) foreground = ta.getDrawable(0)
                    ta.recycle()
                    image = horizontalSquareImageView {
                        adjustViewBounds = true
                    }.lparams(wrapContent, matchParent)
                    view {
                        backgroundResource = R.drawable.gradient_bottom
                    }.lparams(matchParent, matchParent) {
                        gravity = Gravity.BOTTOM
                    }
                    icon = imageView {
                        padding = dip(8)
                    }.lparams(dip(40), dip(40)) {
                        gravity = GravityCompat.START or Gravity.BOTTOM
                        margin = dip(8)
                    }
                }
            }

        }

        class Search(itemView: View, ui: UI, activity: AppCompatActivity) : UniversalViewHolder<MediaObject>(itemView, ui, activity) {

            override fun onClick(v: View?) {
                when (item) {
                    is Album -> ActivityCompat.startActivity(activity, Intent(activity, AlbumActivity::class.java).putExtra("album_id", item!!.id), ActivityOptionsCompat.makeSceneTransitionAnimation(activity).toBundle())
                    is Artist -> ActivityCompat.startActivity(activity, Intent(activity, ArtistActivity::class.java).putExtra("artist_id", item!!.id), ActivityOptionsCompat.makeSceneTransitionAnimation(activity).toBundle())
                    is Playlist -> ActivityCompat.startActivity(activity, Intent(activity, PlaylistActivity::class.java).putExtra("playlist_id", item!!.id), ActivityOptionsCompat.makeSceneTransitionAnimation(activity).toBundle())
                    is Song -> PlaybackRemote.play(item as Song)
                }
            }

            open class UI : UniversalUI() {

                override fun createView(ui: AnkoContext<ViewGroup>) = with(ui) {
                    constraintLayout {
                        val ta = ctx.obtainStyledAttributes(arrayOf(android.R.attr.selectableItemBackground).toIntArray())
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) foreground = ta.getDrawable(0)
                        ta.recycle()
                        isClickable = true

                        image = horizontalSquareImageView {
                            id = 1
                            adjustViewBounds = true
                            scaleType = ImageView.ScaleType.CENTER_CROP
                        }
                        frameLayout {
                            id = 2
                            backgroundResource = R.drawable.gradient_bottom_dark

                            title = textView {
                                rightPadding = dip(16)
                                leftPadding = dip(16)
                                bottomPadding = dip(20)

                                setTextAppearance(context, R.style.ItemTitleTextAppearance)
                                setTextColor(ColorConstants.TEXT_COLOR_DARK_BG)
                                maxLines = 1
                                ellipsize = TextUtils.TruncateAt.END
                            }.lparams(matchParent, wrapContent) {
                                gravity = Gravity.CENTER_VERTICAL
                            }
                            subtitle = textView {
                                rightPadding = dip(16)
                                leftPadding = dip(16)
                                topPadding = dip(20)

                                setTextAppearance(context, R.style.ItemSubtitleTextAppearance)
                                setTextColor(ColorConstants.TEXT_COLOR_SECONDARY_DARK_BG)
                                maxLines = 1
                                ellipsize = TextUtils.TruncateAt.END
                            }.lparams(matchParent, wrapContent) {
                                gravity = Gravity.CENTER_VERTICAL
                            }
                        }

                        val constraints = ConstraintSet()

                        constraints.constrainWidth(1, wrapContent)
                        constraints.connect(1, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 0)
                        constraints.connect(1, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 0)
                        constraints.connect(1, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, 0)

                        constraints.constrainHeight(2, dip(56))
                        constraints.connect(2, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 0)
                        constraints.connect(2, ConstraintSet.END, 1, ConstraintSet.END, 0)
                        constraints.connect(2, ConstraintSet.START, 1, ConstraintSet.START, 0)

                        constraints.applyTo(this)

                    }
                }

                class SingleLine : UI() {

                    override fun createView(ui: AnkoContext<ViewGroup>) = with(ui) {
                        constraintLayout {
                            val ta = ctx.obtainStyledAttributes(arrayOf(android.R.attr.selectableItemBackground).toIntArray())
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) foreground = ta.getDrawable(0)
                            ta.recycle()
                            isClickable = true

                            image = horizontalSquareImageView {
                                id = 1
                                adjustViewBounds = true
                                scaleType = ImageView.ScaleType.CENTER_CROP
                            }
                            title = textView {
                                id = 2
                                backgroundResource = R.drawable.gradient_bottom_dark
                                rightPadding = dip(16)
                                leftPadding = dip(16)
                                gravity = Gravity.CENTER_VERTICAL

                                setTextAppearance(context, R.style.ItemTitleTextAppearance)
                                setTextColor(ColorConstants.TEXT_COLOR_DARK_BG)
                                maxLines = 1
                                ellipsize = TextUtils.TruncateAt.END
                            }

                            val constraints = ConstraintSet()

                            constraints.constrainWidth(1, wrapContent)
                            constraints.connect(1, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 0)
                            constraints.connect(1, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 0)
                            constraints.connect(1, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, 0)

                            constraints.constrainHeight(2, dip(56))
                            constraints.connect(2, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 0)
                            constraints.connect(2, ConstraintSet.END, 1, ConstraintSet.END, 0)
                            constraints.connect(2, ConstraintSet.START, 1, ConstraintSet.START, 0)

                            constraints.applyTo(this)

                        }
                    }

                }

            }

        }

    }

}