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
import android.support.v4.app.ActivityCompat
import android.support.v4.app.ActivityOptionsCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.ViewGroup
import de.julianostarek.music.activities.AlbumActivity
import de.julianostarek.music.activities.ArtistActivity
import de.julianostarek.music.anko.horizontalSquareImageView
import de.julianostarek.music.anko.verticalSquareImageView
import mobile.substance.sdk.music.core.objects.Album
import mobile.substance.sdk.music.core.objects.Artist
import mobile.substance.sdk.music.core.objects.MediaObject
import mobile.substance.sdk.utils.MusicCoreUtil
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.wrapContent

sealed class ImageViewHolders {

    class Horizontal<T : MediaObject>(itemView: View, ui: UniversalUI, activity: AppCompatActivity) : UniversalViewHolder<T>(itemView, ui, activity) {

        override fun onClick(v: View?) {
            when (item) {
                is Album -> ActivityCompat.startActivity(activity, Intent(activity, AlbumActivity::class.java).putExtra("album_id", item!!.id), ActivityOptionsCompat.makeSceneTransitionAnimation(activity).toBundle())
                is Artist -> ActivityCompat.startActivity(activity, Intent(activity, ArtistActivity::class.java).putExtra("artist_id", item!!.id), ActivityOptionsCompat.makeSceneTransitionAnimation(activity).toBundle())
            }
        }

        class UI : UniversalUI() {

            override fun createView(ui: AnkoContext<ViewGroup>) = with(ui) {
                horizontalSquareImageView {
                    image = this
                    layoutParams = ViewGroup.LayoutParams(wrapContent, matchParent)
                    isClickable = true
                    val ta = ctx.obtainStyledAttributes(arrayOf(android.R.attr.selectableItemBackground).toIntArray())
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) foreground = ta.getDrawable(0)
                    ta.recycle()
                }
            }

        }

    }

    class Vertical<T : MediaObject>(itemView: View, ui: UniversalUI, activity: AppCompatActivity) : UniversalViewHolder<T>(itemView, ui, activity) {

        override fun onClick(v: View?) {
            when (item) {
                is Album -> ActivityCompat.startActivity(activity, Intent(activity, AlbumActivity::class.java).putExtra("album_id", item!!.id), ActivityOptionsCompat.makeSceneTransitionAnimation(activity).toBundle())
                is Artist -> ActivityCompat.startActivity(activity, Intent(activity, ArtistActivity::class.java).putExtra("artist_id", item!!.id), ActivityOptionsCompat.makeSceneTransitionAnimation(activity).toBundle())
            }
        }

        class UI : UniversalUI() {

            override fun createView(ui: AnkoContext<ViewGroup>) = with(ui) {
                verticalSquareImageView {
                    image = this
                    layoutParams = ViewGroup.LayoutParams(matchParent, wrapContent)
                    isClickable = true
                    val ta = ctx.obtainStyledAttributes(arrayOf(android.R.attr.selectableItemBackground).toIntArray())
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) foreground = ta.getDrawable(0)
                    ta.recycle()
                }
            }

        }

    }

}