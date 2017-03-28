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
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.support.v4.app.ActivityCompat
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.util.Pair
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import de.julianostarek.music.R
import de.julianostarek.music.activities.AlbumActivity
import de.julianostarek.music.anko.horizontalSquareImageView
import de.julianostarek.music.anko.verticalSquareImageView
import de.julianostarek.music.extensions.isCompletelyVisible
import mobile.substance.sdk.music.core.dataLinkers.MusicData
import mobile.substance.sdk.music.core.objects.Album
import mobile.substance.sdk.music.core.objects.Song
import mobile.substance.sdk.music.playback.PlaybackRemote
import mobile.substance.sdk.utils.MusicCoreUtil
import org.jetbrains.anko.*

open class AlbumViewHolder(itemView: View, ui: UI, activity: AppCompatActivity) : ColorableViewHolder<Album>(itemView, ui, activity) {

    override fun onClick(v: View?) {
        val intent = Intent(activity, AlbumActivity::class.java).putExtra("album_id", item!!.id)
        if (activity.configuration.portrait && isCompletelyVisible()) {
            intent.putExtra("background_color", ((ui as ColorableViewHolder.ColorableUI).colorable.background as ColorDrawable).color)
            ActivityCompat.startActivity(activity,
                    intent,
                    ActivityOptionsCompat.makeSceneTransitionAnimation(activity,
                            Pair.create(ui.image as View, activity.getString(R.string.transition_name_image)),
                            Pair.create(ui.colorable, activity.getString(R.string.transition_name_background))).toBundle())
        } else {
            ActivityCompat.startActivity(activity, intent, ActivityOptionsCompat.makeSceneTransitionAnimation(activity).toBundle())
        }
    }

    class AsSong(itemView: View, ui: UI, activity: AppCompatActivity) : ColorableViewHolder<Song>(itemView, ui, activity) {

        override fun onClick(v: View?) {
            PlaybackRemote.play(MusicData.getSongs(), MusicData.getSongs().indexOf(item))
        }

        class UI : ColorableUI() {

            override fun createView(ui: AnkoContext<ViewGroup>) = with(ui) {
                verticalLayout {
                    layoutParams = ViewGroup.LayoutParams(wrapContent, matchParent)
                    isClickable = true
                    val ta = ctx.obtainStyledAttributes(arrayOf(android.R.attr.selectableItemBackground).toIntArray())
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) foreground = ta.getDrawable(0)
                    ta.recycle()
                    image = horizontalSquareImageView().lparams(wrapContent, matchParent) {
                        weight = 1F
                    }
                    colorable = frameLayout {
                        title = textView {
                            horizontalPadding = dip(16)
                            bottomPadding = dip(20)
                            setTextAppearance(context, R.style.ItemTitleTextAppearance)
                            maxLines = 1
                            ellipsize = TextUtils.TruncateAt.END
                        }.lparams(matchParent, wrapContent) {
                            gravity = Gravity.CENTER_VERTICAL
                        }
                        subtitle = textView {
                            horizontalPadding = dip(16)
                            topPadding = dip(20)
                            setTextAppearance(context, R.style.ItemSubtitleTextAppearance)
                            maxLines = 1
                            ellipsize = TextUtils.TruncateAt.END
                        }.lparams(matchParent, wrapContent) {
                            gravity = Gravity.CENTER_VERTICAL
                        }
                    }.lparams(matchParent, dip(68)) {
                        weight = 0F
                    }
                }
            }
        }

    }

    class UI : ColorableViewHolder.ColorableUI() {

        override fun createView(ui: AnkoContext<ViewGroup>) = with(ui) {
            verticalLayout {
                layoutParams = ViewGroup.LayoutParams(matchParent, wrapContent)
                isClickable = true
                val ta = ctx.obtainStyledAttributes(arrayOf(android.R.attr.selectableItemBackground).toIntArray())
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) foreground = ta.getDrawable(0)
                ta.recycle()
                image = verticalSquareImageView {
                    transitionName = ctx.getString(R.string.transition_name_image)
                }.lparams(matchParent, wrapContent)
                colorable = frameLayout {
                    transitionName = ctx.getString(R.string.transition_name_background)
                    title = textView {
                        horizontalPadding = dip(16)
                        bottomPadding = dip(20)
                        setTextAppearance(context, R.style.ItemTitleTextAppearance)
                        maxLines = 1
                        ellipsize = TextUtils.TruncateAt.END
                    }.lparams(matchParent, wrapContent) {
                        gravity = Gravity.CENTER_VERTICAL
                    }
                    subtitle = textView {
                        horizontalPadding = dip(16)
                        topPadding = dip(20)
                        setTextAppearance(context, R.style.ItemSubtitleTextAppearance)
                        maxLines = 1
                        ellipsize = TextUtils.TruncateAt.END
                    }.lparams(matchParent, wrapContent) {
                        gravity = Gravity.CENTER_VERTICAL
                    }
                }.lparams(matchParent, dip(68))
            }
        }

    }

}