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
import android.support.constraint.ConstraintSet
import android.support.v4.app.ActivityCompat
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import com.afollestad.materialdialogs.MaterialDialog
import de.julianostarek.music.R
import de.julianostarek.music.activities.PlaylistActivity
import de.julianostarek.music.anko.constraintLayout
import de.julianostarek.music.anko.horizontalSquareImageView
import de.julianostarek.music.anko.squareFrameLayout
import de.julianostarek.music.anko.styledButton
import de.julianostarek.music.databases.MetadataHelper
import de.julianostarek.music.extensions.setImageResourceTinted
import de.julianostarek.music.helper.AppColors
import mobile.substance.sdk.colors.ColorConstants
import mobile.substance.sdk.music.core.dataLinkers.MusicData
import mobile.substance.sdk.music.core.objects.Playlist
import mobile.substance.sdk.music.playback.PlaybackRemote
import mobile.substance.sdk.utils.MusicTagsUtil
import org.jetbrains.anko.*
import java.util.*

sealed class PlaylistViewHolders {

    class Normal(itemView: View, ui: UI, activity: AppCompatActivity) : UniversalViewHolder<Playlist>(itemView, ui, activity), View.OnLongClickListener {

        override fun onLongClick(v: View?): Boolean {
            MaterialDialog.Builder(activity)
                    .title(R.string.delete_playlist)
                    .content(R.string.delete_playlist_confirmation)
                    .negativeText(R.string.cancel)
                    .autoDismiss(true)
                    .positiveText(R.string.ok)
                    .onPositive { materialDialog, _ ->
                        materialDialog.dismiss()
                        MetadataHelper.delete(activity, item?.id ?: 0, Playlist::class.java.simpleName)
                        MusicTagsUtil.deletePlaylist(activity, item?.id ?: 0)
                    }
                    .show()
            return true
        }

        override fun onClick(v: View?) {
            when (v) {
                itemView -> ActivityCompat.startActivity(activity, Intent(activity, PlaylistActivity::class.java).putExtra("playlist_id", item!!.id), ActivityOptionsCompat.makeSceneTransitionAnimation(activity).toBundle())
                (ui as Normal.UI).button -> {
                    val songs = MusicData.findSongsForPlaylist(item!!)
                    if (songs.isNotEmpty()) PlaybackRemote.play(songs, 0)
                }
                (ui as Normal.UI).buttonTwo -> {
                    val songs = MusicData.findSongsForPlaylist(item!!)
                    if (songs.isNotEmpty()) {
                        Collections.shuffle(songs)
                        PlaybackRemote.play(songs, 0)
                    }
                }
            }
        }

        init {
            ui.button.setOnClickListener(this)
            ui.buttonTwo.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
        }

        class UI : UniversalUI() {
            lateinit var button: Button
                private set
            lateinit var buttonTwo: Button
                private set

            override fun createView(ui: AnkoContext<ViewGroup>) = with(ui) {
                constraintLayout {
                    val ta = ctx.obtainStyledAttributes(arrayOf(android.R.attr.selectableItemBackground).toIntArray())
                    background = ta.getDrawable(0)
                    ta.recycle()
                    isClickable = true
                    layoutParams = ViewGroup.LayoutParams(matchParent, dip(128))

                    image = horizontalSquareImageView {
                        id = 1
                        adjustViewBounds = true
                        scaleType = ImageView.ScaleType.CENTER_CROP
                    }
                    title = textView {
                        id = 2
                        horizontalPadding = dip(16)
                        topPadding = dip(16)
                        bottomPadding = dip(4)
                        maxLines = 1
                        ellipsize = TextUtils.TruncateAt.END
                        setTextAppearance(context, R.style.ItemTitleTextAppearance)
                    }
                    subtitle = textView {
                        id = 3
                        horizontalPadding = dip(16)
                        bottomPadding = dip(16)
                        maxLines = 1
                        ellipsize = TextUtils.TruncateAt.END
                        setTextAppearance(context, R.style.ItemSubtitleTextAppearance)
                    }
                    button = styledButton(R.style.Widget_AppCompat_Button_Borderless) {
                        id = 4
                        textResource = R.string.play
                    }
                    buttonTwo = styledButton(R.style.Widget_AppCompat_Button_Borderless) {
                        id = 5
                        textResource = R.string.shuffle
                    }

                    val constraints = ConstraintSet()

                    constraints.constrainWidth(1, wrapContent)
                    constraints.connect(1, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 0)
                    constraints.connect(1, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, 0)
                    constraints.connect(1, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 0)

                    constraints.constrainWidth(2, wrapContent)
                    constraints.constrainHeight(2, wrapContent)
                    constraints.connect(2, ConstraintSet.START, 1, ConstraintSet.END, 0)
                    constraints.connect(2, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 0)

                    constraints.constrainWidth(3, wrapContent)
                    constraints.constrainHeight(3, wrapContent)
                    constraints.connect(3, ConstraintSet.START, 1, ConstraintSet.END, 0)
                    constraints.connect(3, ConstraintSet.TOP, 2, ConstraintSet.BOTTOM, 0)

                    constraints.constrainWidth(4, wrapContent)
                    constraints.constrainHeight(4, wrapContent)
                    constraints.connect(4, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, 0)
                    constraints.connect(4, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 0)

                    constraints.constrainWidth(5, wrapContent)
                    constraints.constrainHeight(5, wrapContent)
                    constraints.connect(5, ConstraintSet.END, 4, ConstraintSet.START, 0)
                    constraints.connect(5, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 0)

                    constraints.applyTo(this)
                }
            }

        }

    }

    class Small(itemView: View, ui: UI, activity: AppCompatActivity) : UniversalViewHolder<Playlist>(itemView, ui, activity) {

        override fun onClick(v: View?) {
            ActivityCompat.startActivity(activity, Intent(activity, PlaylistActivity::class.java).putExtra("playlist_id", item!!.id), ActivityOptionsCompat.makeSceneTransitionAnimation(activity).toBundle())
        }

        class UI : UniversalUI() {
            lateinit var icon: ImageView
                private set

            override fun createView(ui: AnkoContext<ViewGroup>) = with(ui) {
                squareFrameLayout {
                    backgroundColor = AppColors.ACCENT_COLOR
                    layoutParams = ViewGroup.LayoutParams(wrapContent, matchParent)

                    title = textView {
                        padding = dip(16)
                        setTextAppearance(context, R.style.ItemTitleTextAppearance)
                        maxLines = 2
                        ellipsize = TextUtils.TruncateAt.END
                        textColor = ContextCompat.getColor(ctx, R.color.primary_text_color_dark)

                        layoutParams = FrameLayout.LayoutParams(matchParent, wrapContent)
                    }
                    icon = imageView {
                        padding = dip(8)
                        setImageResourceTinted(R.drawable.ic_queue_music_black_24dp, ColorConstants.ICON_COLOR_ACTIVE_DARK_BG)

                        layoutParams = FrameLayout.LayoutParams(dip(40), dip(40))
                        (layoutParams as FrameLayout.LayoutParams).gravity = GravityCompat.START or Gravity.BOTTOM
                        (layoutParams as FrameLayout.LayoutParams).margin = dip(8)
                    }
                }
            }

        }

    }

}