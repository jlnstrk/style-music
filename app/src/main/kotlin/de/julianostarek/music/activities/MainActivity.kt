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

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.app.Activity
import android.app.SearchManager
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.provider.MediaStore
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.NavigationView
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import butterknife.bindView
import butterknife.bindViews
import co.metalab.asyncawait.async
import com.afollestad.assent.Assent
import com.afollestad.assent.AssentCallback
import com.bumptech.glide.Glide
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.target.Target
import de.julianostarek.music.R
import de.julianostarek.music.StyleMusic
import de.julianostarek.music.drawable.PlayPauseDrawable
import de.julianostarek.music.extensions.*
import de.julianostarek.music.fragments.*
import de.julianostarek.music.helper.AppColors
import de.julianostarek.music.helper.PreferenceConstants
import de.julianostarek.music.helper.RequestCodes
import de.julianostarek.music.views.CustomBottomSheetBehavior
import io.github.mthli.slice.Slice
import mobile.substance.sdk.colors.ColorConstants
import mobile.substance.sdk.colors.ColorPackage
import mobile.substance.sdk.colors.DynamicColors
import mobile.substance.sdk.colors.DynamicColorsCallback
import mobile.substance.sdk.music.core.dataLinkers.MusicData
import mobile.substance.sdk.music.core.objects.Song
import mobile.substance.sdk.music.loading.Library
import mobile.substance.sdk.music.loading.LibraryConfig
import mobile.substance.sdk.music.loading.MusicType
import mobile.substance.sdk.music.playback.PlaybackRemote
import mobile.substance.sdk.options.MusicCoreOptions
import org.jetbrains.anko.defaultSharedPreferences
import org.jetbrains.anko.dip

class MainActivity : PlaybackRemoteActivity(), NavigationView.OnNavigationItemSelectedListener, DynamicColorsCallback {
    val contentRoot: CoordinatorLayout by bindView<CoordinatorLayout>(R.id.activity_main_content_root)
    val bottomSheetBehavior: CustomBottomSheetBehavior<CoordinatorLayout> by lazy {
        val behavior = CustomBottomSheetBehavior<CoordinatorLayout>()
        (nowPlayingFragment.view!!.layoutParams as CoordinatorLayout.LayoutParams).behavior = behavior
        behavior
    }
    val drawerLayout: DrawerLayout by bindView<DrawerLayout>(R.id.activity_main_drawerlayout)
    private val navigationView: NavigationView by bindView<NavigationView>(R.id.activity_main_navigation_view)
    private val textViews: List<TextView> by bindViews<TextView>(R.id.activity_main_card_title, R.id.activity_main_card_subtitle)
    private val collapsedBar: FrameLayout by bindView<FrameLayout>(R.id.activity_main_dragview_card)
    val collapsedBarProgressBar: ProgressBar by bindView<ProgressBar>(R.id.activity_main_card_progressbar)
    val artwork: ImageView by bindView<ImageView>(R.id.activity_main_card_artwork)
    private val playPause: ImageView by bindView<ImageView>(R.id.activity_main_card_playpause)
    private val evaluator = ArgbEvaluator()
    var playPauseDrawable: PlayPauseDrawable? = null
    var tintColor: Int = ColorConstants.ICON_COLOR_ACTIVE_LIGHT_BG
    val nowPlayingFragment: NowPlayingFragment by lazy {
        supportFragmentManager.findFragmentById(R.id.activity_main_fragment_nowplaying) as NowPlayingFragment
    }
    var fragment: Fragment? = null

    companion object {
        const val KEY_FRAGMENT = "fragment"
    }

    inner class BottomSheetCallback : BottomSheetBehavior.BottomSheetCallback() {

        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            if (slideOffset >= 0) {
                collapsedBar.alpha = 1.0F - slideOffset
                val color = evaluator.evaluate(slideOffset, AppColors.PRIMARY_DARK_COLOR, tintColor) as Int
                drawerLayout.setStatusBarBackgroundColor(color)
                invalidateStatusBarIcons(color.isLight())
            }
        }

        override fun onStateChanged(bottomSheet: View, newState: Int) {
            if (newState != BottomSheetBehavior.STATE_HIDDEN) bottomSheetBehavior.isHideable = false
            when (newState) {
                BottomSheetBehavior.STATE_DRAGGING, BottomSheetBehavior.STATE_SETTLING, BottomSheetBehavior.STATE_COLLAPSED -> if (collapsedBar.visibility == View.GONE) collapsedBar.visibility = View.VISIBLE
                BottomSheetBehavior.STATE_EXPANDED -> collapsedBar.visibility = View.GONE
            }
        }

    }

    fun app() = application as StyleMusic

    override fun onColorsReady(colors: ColorPackage) {
        val tintColor = if (!colors.accentColor.isLight()) colors.accentColor else if (!colors.primaryColor.isLight()) colors.primaryColor else colors.primaryDarkColor
        val animator = ValueAnimator.ofArgb(this.tintColor, tintColor)
        animator.duration = 500
        animator.addUpdateListener {
            val tintList = ColorStateList.valueOf(animator.animatedValue as Int)
            nowPlayingFragment.tintWith(tintList)
            collapsedBarProgressBar.progressTintList = tintList
            invalidateStatusBarColor(animator.animatedValue as Int)
        }
        animator.start()
        this.tintColor = tintColor
    }

    fun onQueueSlide(fraction: Float) = invalidateStatusBarColor(evaluator.evaluate(fraction, tintColor, AppColors.PRIMARY_DARK_COLOR) as Int, true)

    ///////////////////////////////////////////////////////////////////////////
    // Lifecycle
    ///////////////////////////////////////////////////////////////////////////

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Assent.setActivity(this, this)
        setContentView(R.layout.activity_main)

        bottomSheetBehavior.apply {
            isHideable = true
            state = BottomSheetBehavior.STATE_HIDDEN
            peekHeight = dip(56)
            setBottomSheetCallback(BottomSheetCallback())
        }
        Slice(nowPlayingFragment.view).apply {
            showLeftBottomRect(false)
            showRightBottomRect(false)
            showLeftTopRect(false)
            showRightTopRect(false)
            setElevation(16F)
        }

        async {
            if (!await { hasIntroBeenShown() }) {
                val i = Intent(this@MainActivity, AppIntroActivity::class.java)
                startActivityForResult(i, RequestCodes.INTRO_REQUEST_CODE)
            } else if (!Assent.isPermissionGranted(Assent.READ_EXTERNAL_STORAGE) || !Assent.isPermissionGranted(Assent.WRITE_EXTERNAL_STORAGE)) {
                Assent.requestPermissions(AssentCallback {
                    result ->
                    if (result!!.allPermissionsGranted()) init(savedInstanceState)
                }, RequestCodes.PERMISSIONS_REQUEST_CODE, Assent.READ_EXTERNAL_STORAGE, Assent.WRITE_EXTERNAL_STORAGE)
            } else init(savedInstanceState)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RequestCodes.INTRO_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                setIntroShown()
                init(null)
            } else finish()
        }
    }

    override fun onResume() {
        super.onResume()
        Assent.setActivity(this, this)
        Library.registerBuildFinishedListener({
            handleIntent()
        }, true)
    }

    override fun onPause() {
        super.onPause()
        if (isFinishing)
            Assent.setActivity(this, null)
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
            if (nowPlayingFragment.bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
                nowPlayingFragment.bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            } else bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        } else if (!(fragment is StyleFolderBrowserFragment && !(fragment as StyleFolderBrowserFragment).navigateUp())) finish()
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        try {
            fragment = supportFragmentManager.getFragment(savedInstanceState, KEY_FRAGMENT)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) = Assent.handleResult(permissions, grantResults)

    override fun onSaveInstanceState(outState: Bundle) {
        if (Assent.isPermissionGranted(Assent.READ_EXTERNAL_STORAGE) && fragment != null) {
            super.onSaveInstanceState(outState)
            supportFragmentManager.putFragment(outState, KEY_FRAGMENT, fragment)
        }
    }

    private fun handleIntent() {
        when (intent.action) {
            MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH -> PlaybackRemote.playFromSearch(intent.getStringExtra(SearchManager.QUERY), intent.extras)
            Intent.ACTION_VIEW -> PlaybackRemote.playFromUri(intent.data, intent.extras ?: Bundle.EMPTY)
            "de.julianostarek.music.action.SHUFFLE" -> PlaybackRemote.shuffle()
            "de.julianostarek.music.action.PLAY_LAST_ADDED" -> PlaybackRemote.play(MusicData.getSongs().sortedByDescending(Song::dateAdded), 0)
            "de.julianostarek.music.action.PLAY_FAVORITES" -> async {
                val favorites = await { getFavorites<Song>(this@MainActivity, MusicType.SONGS) }
                if (favorites.isNotEmpty()) PlaybackRemote.play(favorites, 0)
            }
        }
        intent.action = null
    }

    ///////////////////////////////////////////////////////////////////////////
    // Playback callbacks
    ///////////////////////////////////////////////////////////////////////////

    override fun onSongChanged(song: Song) {
        if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_HIDDEN) {
            if (fragment is RadioFragment) (fragment as RadioFragment).UI.floatingActionButton.animate().setDuration(200).translationY(0F).start()
            if (fragment is LibraryFragment) (fragment as LibraryFragment).UI.floatingActionButton.animate().setDuration(200).translationY(0F).start()
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        textViews[0].text = song.songTitle
        textViews[1].text = song.songArtistName

        val isRadio = song.getData("is_radio") != null

        nowPlayingFragment.seekBars[0].thumb.alpha = if (isRadio) 0 else 255
        nowPlayingFragment.seekBars[0].isEnabled = !isRadio

        Glide.with(this@MainActivity)
                .load(if (isRadio) song.explicitArtworkUri.toString() else MusicData.findAlbumById(song.songAlbumId ?: 0)?.albumArtworkPath.orEmpty())
                .asBitmap()
                .error(MusicCoreOptions.defaultArt)
                .into(object : SimpleTarget<Bitmap>(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL) {
                    override fun onResourceReady(resource: Bitmap, glideAnimation: GlideAnimation<in Bitmap>?) {
                        artwork.setImageBitmap(resource)
                        nowPlayingFragment.artwork.setImageBitmap(resource)
                        DynamicColors.from(resource).generate(true, this@MainActivity)
                    }

                    override fun onLoadFailed(e: java.lang.Exception?, errorDrawable: Drawable?) {
                        artwork.setImageDrawable(errorDrawable)
                        nowPlayingFragment.artwork.setImageDrawable(errorDrawable)
                        DynamicColors.from(resources, MusicCoreOptions.defaultArt).generate(true, this@MainActivity)
                    }
                })

    }

    override fun onDurationChanged(duration: Int, durationString: String) {
        collapsedBarProgressBar.max = duration
    }

    override fun onProgressChanged(progress: Int) {
        collapsedBarProgressBar.progress = progress
    }

    override fun onStateChanged(state: Int) {
        when (state) {
            PlaybackStateCompat.STATE_PAUSED, PlaybackStateCompat.STATE_NONE -> playPauseDrawable?.setPlay(true)
            PlaybackStateCompat.STATE_PLAYING -> playPauseDrawable?.setPause(true)
        }
    }

    override fun onRepeatModeChanged(@PlaybackStateCompat.RepeatMode mode: Int) {
    }

    override fun onQueueChanged(queue: List<Song>) {
    }

    ///////////////////////////////////////////////////////////////////////////
    // UI
    ///////////////////////////////////////////////////////////////////////////

    fun init(savedInstanceState: Bundle?) {
        Library.init(this, LibraryConfig()
                .load(MusicType.ALBUMS,
                        MusicType.SONGS,
                        MusicType.ARTISTS,
                        MusicType.PLAYLISTS))
        Library.registerArtistListener(app())
        if (!Library.isBuilt()) Library.build()

        if (savedInstanceState == null) handleLaunch() else if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
            collapsedBar.visibility = View.GONE
            collapsedBar.alpha = 0F
        }
        initUI()
    }

    fun initUI() {
        navigationView.setNavigationItemSelectedListener(this)
        collapsedBar.setOnClickListener { bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED }
        playPause.setOnClickListener {
            when (playPauseDrawable!!.playPauseState) {
                PlayPauseDrawable.STATE_PLAY -> PlaybackRemote.resume()
                PlayPauseDrawable.STATE_PAUSE -> PlaybackRemote.pause()
            }
        }
        initPlayPauseDrawables()
    }

    fun initPlayPauseDrawables() {
        playPauseDrawable = PlayPauseDrawable(this, ColorConstants.ICON_COLOR_ACTIVE_LIGHT_BG)
        playPause.setImageDrawable(playPauseDrawable)
    }

    override fun onNavigationItemSelected(it: MenuItem): Boolean {
        drawerLayout.closeDrawer(GravityCompat.START)

        when (it.itemId) {
            R.id.nav_home -> {
                it.isChecked = true
                if (fragment !is HomeFragment) {
                    fragment = HomeFragment()
                    commitFragment()
                    return true
                } else return false
            }
            R.id.nav_library -> {
                it.isChecked = true
                if (fragment !is LibraryFragment) {
                    fragment = LibraryFragment()
                    commitFragment()
                    return true
                } else return false
            }
            R.id.nav_folder_browser -> {
                it.isChecked = true
                if (fragment !is StyleFolderBrowserFragment) {
                    fragment = StyleFolderBrowserFragment()
                    commitFragment()
                    return true
                } else return false
            }
            R.id.nav_radio -> {
                if (isNetworkAvailable()) {
                    it.isChecked = true
                    if (fragment !is RadioFragment) {
                        fragment = RadioFragment()
                        commitFragment()
                        return true
                    } else return false
                } else {
                    Snackbar.make(contentRoot, getString(R.string.no_network), Snackbar.LENGTH_SHORT).show()
                    return false
                }
            }
            R.id.nav_about -> {
                startActivity(Intent(this, AboutActivity::class.java))
                return true
            }
            R.id.nav_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                return true
            }
            else -> return false
        }
    }

    fun commitFragment() {
        supportFragmentManager.beginTransaction()
                .replace(R.id.activity_main_fragment_placeholder, fragment)
                .commitNow()
    }

    private fun handleLaunch() {
        val preference = defaultSharedPreferences.getString(PreferenceConstants.PREF_KEY_START_PAGE, "0").toInt()
        navigationView.menu.getItem(preference).isChecked = true

        when (preference) {
            0 -> fragment = HomeFragment()
            1 -> fragment = LibraryFragment()
            2 -> fragment = StyleFolderBrowserFragment()
        }

        commitFragment()
    }

    ///////////////////////////////////////////////////////////////////////////
    // System bars
    ///////////////////////////////////////////////////////////////////////////

    fun invalidateStatusBarColor(color: Int, ignoreQueue: Boolean = false) {
        if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED && (ignoreQueue || nowPlayingFragment.bottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED)) {
            drawerLayout.setStatusBarBackgroundColor(color)
            invalidateStatusBarIcons(color.isLight())
        }
    }

    fun invalidateStatusBarIcons(lightStatus: Boolean) {
        window.decorView.systemUiVisibility = if (lightStatus) window.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR else window.decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
    }

}



