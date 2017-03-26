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

package de.julianostarek.music.activities

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.SharedElementCallback
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.FloatingActionButton
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.transition.Transition
import android.view.MenuItem
import android.view.View
import android.view.ViewTreeObserver
import android.widget.ImageView
import butterknife.bindOptionalView
import butterknife.bindView
import co.metalab.asyncawait.async
import de.julianostarek.music.R
import de.julianostarek.music.extensions.*
import de.julianostarek.music.helper.AppColors
import de.julianostarek.music.helper.DragDisabledCallback
import mobile.substance.sdk.colors.ColorConstants
import mobile.substance.sdk.colors.ColorPackage
import mobile.substance.sdk.colors.DynamicColors
import mobile.substance.sdk.colors.DynamicColorsCallback
import mobile.substance.sdk.music.core.objects.MediaObject
import mobile.substance.sdk.music.core.objects.Playlist
import mobile.substance.sdk.options.DynamicColorsOptions
import org.jetbrains.anko.configuration
import org.jetbrains.anko.imageResource

abstract class ItemDetailActivity<T : MediaObject, VH : RecyclerView.ViewHolder> : PlaybackRemoteActivity(), Toolbar.OnMenuItemClickListener, View.OnClickListener, Transition.TransitionListener, DynamicColorsCallback {
    var colors: ColorPackage = DynamicColorsOptions.defaultColors
    val toolbar: Toolbar by bindView<Toolbar>(R.id.activity_item_detail_toolbar)
    val floatingActionButton: FloatingActionButton by bindView<FloatingActionButton>(R.id.activity_item_detail_fab)
    val appBarLayout: AppBarLayout? by bindOptionalView<AppBarLayout>(R.id.activity_item_detail_appbar_layout)
    val recyclerView: RecyclerView by bindView<RecyclerView>(R.id.activity_item_detail_recycler_view)
    val image: ImageView by bindView<ImageView>(R.id.activity_item_detail_appbar_image)
    val gradient: View by bindView<View>(R.id.activity_item_detail_image_gradient_top)
    lateinit var mediaObject: T
    open var advancedTransition = false

    override fun onColorsReady(colors: ColorPackage) {
        this.colors = colors
        window.statusBarColor = colors.accentColor
        if (colors.accentColor.isLight())
            window.decorView.systemUiVisibility = window.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        floatingActionButton.backgroundTintList = ColorStateList.valueOf(colors.accentColor)
        floatingActionButton.setImageResourceTinted(R.drawable.ic_play_arrow_black_24dp, colors.accentIconActiveColor)
        try {
            (recyclerView.getChildAt(0).findViewById(1) as ImageView).setImageResourceTinted(R.drawable.ic_shuffle_white_24dp, colors.primaryDarkColor)
        } catch (e: Exception) {
        }
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.item_add_to_favorites -> {
                toggleIsFavorite()
                return true
            }
            else -> return false
        }
    }

    private fun invalidateIsFavorite() {
        async {
            toolbar.menu.findItem(R.id.item_add_to_favorites).setIcon(if (await {
                mediaObject.isFavorite(this@ItemDetailActivity)
            }) R.drawable.ic_favorite_white_24dp else R.drawable.ic_favorite_border_white_24dp)
        }
    }

    private fun toggleIsFavorite() = async {
        val isFavorite = await { mediaObject.isFavorite(this@ItemDetailActivity) }
        val finalValue = await { mediaObject.setFavorite(this@ItemDetailActivity, !isFavorite) }
        toolbar.menu.findItem(R.id.item_add_to_favorites).icon = ContextCompat.getDrawable(this@ItemDetailActivity, if (finalValue) R.drawable.ic_favorite_white_24dp else R.drawable.ic_favorite_border_white_24dp)
    }

    override fun onBackPressed() {
        floatingActionButton.animate().scaleX(0.0F).scaleY(0.0F).setDuration(200).setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                floatingActionButton.visibility = View.INVISIBLE
                try {
                    super@ItemDetailActivity.onBackPressed()
                } catch (ignored: Exception) {
                }
            }
        }).start()
    }

    override fun onTransitionEnd(p0: Transition?) = floatingActionButton.animate().scaleX(1.0F).scaleY(1.0F).setDuration(200).start()

    override fun onTransitionResume(p0: Transition?) {

    }

    override fun onTransitionPause(p0: Transition?) {

    }

    override fun onTransitionCancel(p0: Transition?) {

    }

    override fun onTransitionStart(p0: Transition?) {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_detail)
        postponeEnterTransition()
        mediaObject = findMediaObject(intent)
        init(savedInstanceState)
    }

    private fun init(savedInstanceState: Bundle?) {
        if (appBarLayout != null) {
            val behavior = AppBarLayout.Behavior()
            behavior.setDragCallback(DragDisabledCallback())
            (appBarLayout!!.layoutParams as CoordinatorLayout.LayoutParams).behavior = behavior
        }
        toolbar.inflateMenu(menuResId)

        val dynamicColors = setArtwork(image)
        if (dynamicColors != null) {
            generateColors(dynamicColors)
            initTransitions(savedInstanceState, true)
        } else {
            setDefaultArtwork()
            initTransitions(savedInstanceState, false)
        }

        toolbar.setNavigationOnClickListener(this)
        toolbar.setOnMenuItemClickListener(this)
        toolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_arrow_back_black_24dp).tinted(ColorConstants.ICON_COLOR_ACTIVE_DARK_BG)
        floatingActionButton.setOnClickListener(this)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = getAdapter()
        window.decorView.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                window.decorView.viewTreeObserver.removeOnPreDrawListener(this)
                startPostponedEnterTransition()
                return true
            }
        })

        if (mediaObject !is Playlist) invalidateIsFavorite()
    }

    fun initTransitions(savedInstanceState: Bundle?, hasArtwork: Boolean) {
        window.enterTransition.addListener(this)
        if (intent.hasExtra("background_color") && configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            advancedTransition = true
            val backgroundColor = intent.getIntExtra("background_color", Color.WHITE)
            setEnterSharedElementCallback(object : SharedElementCallback() {
                override fun onSharedElementStart(sharedElementNames: MutableList<String>?, sharedElements: MutableList<View>?, sharedElementSnapshots: MutableList<View>?) {
                    try {
                        sharedElements!![1].setBackgroundColor(backgroundColor)
                    } catch (ignored: Exception) {
                    }
                }

                override fun onSharedElementEnd(sharedElementNames: MutableList<String>?, sharedElements: MutableList<View>?, sharedElementSnapshots: MutableList<View>?) {
                    try {
                        sharedElements!![1].setBackgroundColor(if (hasArtwork) AppColors.PRIMARY_LIGHT_BACKGROUND else AppColors.PRIMARY_LIGHT_BACKGROUND_LIGHT)
                    } catch (ignored: Exception) {
                    }
                }
            })
            setExitSharedElementCallback(object : SharedElementCallback() {
                override fun onSharedElementEnd(sharedElementNames: MutableList<String>?, sharedElements: MutableList<View>?, sharedElementSnapshots: MutableList<View>?) {
                    try {
                        sharedElements!![1].setBackgroundColor(backgroundColor)
                    } catch (ignored: Exception) {
                    }
                }
            })
        }

        if (savedInstanceState != null) {
            floatingActionButton.scaleY = 1.0F
            floatingActionButton.scaleX = 1.0F
        }
    }

    abstract fun getAdapter(): RecyclerView.Adapter<VH>

    abstract fun setArtwork(imageView: ImageView): DynamicColors?

    private fun generateColors(dynamicColors: DynamicColors) {
        dynamicColors.generate(true, this)
    }

    private fun setDefaultArtwork() {
        image.imageResource = defaultArtworkResId
        onColorsReady(DynamicColorsOptions.defaultColors)
    }

    abstract val defaultArtworkResId: Int

    abstract val menuResId: Int

    abstract fun findMediaObject(intent: Intent): T

}