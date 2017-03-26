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

package de.julianostarek.music.fragments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.media.AudioManager
import android.media.session.PlaybackState
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.CoordinatorLayout
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.content.ContextCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v4.view.GravityCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.CardView
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.support.v7.widget.helper.ItemTouchHelper
import android.util.DisplayMetrics
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import butterknife.bindOptionalViews
import butterknife.bindView
import butterknife.bindViews
import co.metalab.asyncawait.async
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.gms.cast.framework.CastButtonFactory
import de.julianostarek.music.R
import de.julianostarek.music.activities.AlbumActivity
import de.julianostarek.music.activities.ArtistActivity
import de.julianostarek.music.activities.MainActivity
import de.julianostarek.music.adapters.QueueAdapter
import de.julianostarek.music.anko.viewholders.QueueItemViewHolder
import de.julianostarek.music.drawable.PlayPauseDrawable
import de.julianostarek.music.extensions.isFavorite
import de.julianostarek.music.extensions.setFavorite
import de.julianostarek.music.extensions.tinted
import de.julianostarek.music.helper.PreferenceConstants
import mobile.substance.sdk.colors.ColorConstants
import mobile.substance.sdk.colors.ColorPackage
import mobile.substance.sdk.colors.DynamicColorsCallback
import mobile.substance.sdk.music.core.objects.Song
import mobile.substance.sdk.music.playback.PlaybackRemote
import mobile.substance.sdk.utils.MusicCoreUtil
import org.jetbrains.anko.support.v4.defaultSharedPreferences
import org.jetbrains.anko.support.v4.dip
import org.jetbrains.anko.support.v4.sp
import org.jetbrains.anko.topPadding

class NowPlayingFragment : BaseFragment(), Toolbar.OnMenuItemClickListener, SeekBar.OnSeekBarChangeListener, PlaybackRemote.RemoteCallback, DynamicColorsCallback, View.OnClickListener {
    val seekBars: List<SeekBar> by bindViews<SeekBar>(R.id.fragment_nowplaying_progress_seekbar, R.id.fragment_nowplaying_volume_seekbar)
    val progressTexts: List<TextView> by bindViews<TextView>(R.id.fragment_nowplaying_progress_text, R.id.fragment_nowplaying_duration_text)
    val imageViews: List<ImageView> by bindViews<ImageView>(R.id.fragment_nowplaying_loop,
            R.id.fragment_nowplaying_skip_back, R.id.fragment_nowplaying_playpause,
            R.id.fragment_nowplaying_skip_forward, R.id.fragment_nowplaying_shuffle,
            R.id.fragment_nowplaying_volume_down, R.id.fragment_nowplaying_volume_up)
    val artwork: ImageView by bindView<ImageView>(R.id.fragment_nowplaying_artwork)
    val toolbar: Toolbar by bindView<Toolbar>(R.id.fragment_nowplaying_toolbar)
    var playPauseDrawable: PlayPauseDrawable? = null

    val playingTitle: TextView by bindView<TextView>(R.id.fragment_nowplaying_queue_playing_title)
    val playingSubtitle: TextView by bindView<TextView>(R.id.fragment_nowplaying_queue_playing_subtitle)
    val playArrowOutlineIcon: ImageView by bindView<ImageView>(R.id.fragment_nowplaying_queue_playing_play_circle_colorable)
    val playingDuration: TextView by bindView<TextView>(R.id.fragment_nowplaying_queue_playing_duration)
    val playing: TextView by bindView<TextView>(R.id.fragment_nowplaying_queue_playing)

    val controlsConstraintLayout: ConstraintLayout by bindView<ConstraintLayout>(R.id.fragment_nowplaying_controls_viewgroup)
    val queueCardView: CardView by bindView<CardView>(R.id.fragment_nowplaying_queue_layout)
    val bottomSheetBehavior: BottomSheetBehavior<CardView> by lazy {
        BottomSheetBehavior.from(queueCardView)
    }

    val queue: RecyclerView by bindView<RecyclerView>(R.id.fragment_nowplaying_queue)
    var touchHelper: ItemTouchHelper? = null

    var audioManager: AudioManager? = null
    val volumeChangeListener = VolumeChangeListener()

    inner class BottomSheetCallbackImpl : BottomSheetBehavior.BottomSheetCallback() {
        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            mainActivity().onQueueSlide(slideOffset)
            queueCardView.elevation = if (slideOffset == 0F) 0F else dip(slideOffset * 8F).toFloat() + dip(8)
            (playArrowOutlineIcon.parent as ConstraintLayout).topPadding = (slideOffset * (dip(32) + sp(16))).toInt()
        }

        override fun onStateChanged(bottomSheet: View, newState: Int) {
            playing.animate()
                    .alpha(if (newState == BottomSheetBehavior.STATE_EXPANDED) 1.0F else 0.0F)
                    .setDuration(200)
                    .start()
            mainActivity().bottomSheetBehavior.isTouchInterceptionEnabled = newState == BottomSheetBehavior.STATE_COLLAPSED
        }

    }

    override fun onClick(v: View?) {
        when (v) {
            imageViews[0] -> PlaybackRemote.switchRepeatMode()
            imageViews[1] -> PlaybackRemote.playPrevious()
            imageViews[2] -> if (PlaybackRemote.isPlaying() ?: false) PlaybackRemote.pause() else PlaybackRemote.resume()
            imageViews[3] -> PlaybackRemote.playNext()
            imageViews[4] -> PlaybackRemote.useShuffledQueue(true)
            imageViews[5] -> audioManager?.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, 0)
            imageViews[6] -> audioManager?.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, 0)
            queueCardView -> bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    override fun onColorsReady(colors: ColorPackage) {

    }

    override fun onError() {

    }

    override fun onQueueChanged(queue: List<Song>) {
        (this.queue.adapter as QueueAdapter).onQueueChanged()
    }

    override fun onStateChanged(state: Int) {
        when (state) {
            PlaybackStateCompat.STATE_PLAYING, PlaybackStateCompat.STATE_BUFFERING, PlaybackStateCompat.STATE_CONNECTING -> playPauseDrawable?.setPause(true)
            else -> playPauseDrawable?.setPlay(true)
        }
    }

    fun tintWith(tintList: ColorStateList) {
        seekBars[0].thumbTintList = tintList
        seekBars[1].progressTintList = tintList
        seekBars[1].thumbTintList = tintList
        imageViews[2].backgroundTintList = tintList
        playArrowOutlineIcon.imageTintList = tintList
        if (PlaybackRemote.getRepeatMode() != PlaybackStateCompat.REPEAT_MODE_NONE) imageViews[0].imageTintList = tintList
        (activity as MainActivity).collapsedBarProgressBar.progressTintList = tintList
    }

    override fun onRepeatModeChanged(@PlaybackStateCompat.RepeatMode mode: Int) {
        var resId: Int? = null
        when (mode) {
            PlaybackStateCompat.REPEAT_MODE_ALL -> resId = R.drawable.ic_repeat_black_24dp
            PlaybackStateCompat.REPEAT_MODE_NONE -> resId = R.drawable.ic_repeat_black_transparent_disabled_24dp
            PlaybackStateCompat.REPEAT_MODE_ONE -> resId = R.drawable.ic_repeat_one_black_24dp
        }
        imageViews[0].setImageDrawable(ContextCompat.getDrawable(activity, resId!!))
        imageViews[0].imageTintList = if (mode != PlaybackStateCompat.REPEAT_MODE_NONE) ColorStateList.valueOf(mainActivity().tintColor) else null
    }

    override fun init() {
        initToolbar()
        initQueue()

        val isSmallScreenMode = defaultSharedPreferences.getBoolean(PreferenceConstants.PREF_KEY_SMALL_SCREEN_MODE, false)

        initControls(isSmallScreenMode)
        initBottomSheet(isSmallScreenMode)

        audioManager = activity.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        activity.registerReceiver(volumeChangeListener, IntentFilter("android.media.VOLUME_CHANGED_ACTION"))
        initSeekBars()
    }

    private fun initBottomSheet(isSmallScreenMode: Boolean) {
        queueCardView.setOnClickListener(this)
        val isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        if (isLandscape) {
            (queueCardView.layoutParams as CoordinatorLayout.LayoutParams).gravity = GravityCompat.END
            queueCardView.layoutParams.width = dip(324)
        }
        bottomSheetBehavior.apply {
            isHideable = false
            skipCollapsed = false
            state = BottomSheetBehavior.STATE_COLLAPSED
            setBottomSheetCallback(BottomSheetCallbackImpl())
            val realDisplayMetrics = DisplayMetrics()
            activity.windowManager.defaultDisplay.getMetrics(realDisplayMetrics)
            if (!isLandscape) {
                peekHeight = realDisplayMetrics.heightPixels - dip(186 - if (isSmallScreenMode) 52 else 0) - realDisplayMetrics.widthPixels
            } else {
                peekHeight = realDisplayMetrics.heightPixels - dip(186 - if (isSmallScreenMode) 52 else 0)
            }
        }
    }

    private fun initQueue() {
        touchHelper = ItemTouchHelper(DragDropCallback())
        touchHelper?.attachToRecyclerView(queue.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = QueueAdapter({
                touchHelper?.startDrag(it)
            }, activity as AppCompatActivity)
        })
    }

    inner class DragDropCallback : ItemTouchHelper.Callback() {
        override fun getMovementFlags(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?): Int {
            return makeMovementFlags(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0)
        }

        override fun onMove(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?, target: RecyclerView.ViewHolder?): Boolean {
            if (viewHolder!!::class.java == target!!::class.java) {
                val positions = arrayOf(viewHolder.adapterPosition, target.adapterPosition)
                queue.adapter.notifyItemMoved(positions[0], positions[1])
                PlaybackRemote.swapQueueItems(positions[0], positions[1], true)
                for (i in (recyclerView!!.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()..(recyclerView.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()) {
                    ((recyclerView.findViewHolderForAdapterPosition(i) as QueueItemViewHolder).ui as QueueItemViewHolder.UI).index.text = (i + 2).toString()
                }
                return true
            } else return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder?, direction: Int) {
        }


        override fun isLongPressDragEnabled(): Boolean {
            return false
        }

        override fun isItemViewSwipeEnabled(): Boolean {
            return false
        }
    }

    private fun initToolbar() {
        toolbar.apply {
            navigationIcon = ContextCompat.getDrawable(activity, R.drawable.ic_expand_more_white_24dp).tinted(ColorConstants.ICON_COLOR_ACTIVE_DARK_BG)
            inflateMenu(R.menu.menu_nowplaying)
            setNavigationOnClickListener { (activity as MainActivity).bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED }
            setOnMenuItemClickListener(this@NowPlayingFragment)
        }
        try {
            CastButtonFactory.setUpMediaRouteButton(activity, toolbar.menu, R.id.item_cast)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(activity, "Google Cast will not work on this device. Google Play Services are not installed or too old", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initControls(isSmallScreenMode: Boolean) {
        if (isSmallScreenMode) {
            controlsConstraintLayout.layoutParams.height -= dip(52)
            imageViews[5].isEnabled = false
            imageViews[6].isEnabled = false
        }
        playPauseDrawable = PlayPauseDrawable(activity, ColorConstants.ICON_COLOR_ACTIVE_DARK_BG)
        imageViews[2].setImageDrawable(playPauseDrawable)
        imageViews.forEach { it.setOnClickListener(this) }

        playingTitle.isSelected = true
        playingSubtitle.isSelected = true
    }

    private fun initSeekBars() {
        seekBars.forEach { it.setOnSeekBarChangeListener(this) }
        seekBars[1].apply {
            max = audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC) ?: 0
            progress = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: 0
        }
    }

    override fun onDurationChanged(duration: Int, durationString: String) {
        progressTexts[1].text = durationString
        seekBars[0]?.max = duration
    }

    override fun onProgressChanged(progress: Int) {
        progressTexts[0].text = MusicCoreUtil.stringForTime(progress.toLong())
        seekBars[0]?.progress = progress
    }

    override fun onSongChanged(song: Song) {
        playingTitle.text = song.songTitle
        playingSubtitle.text = song.songArtistName
        playingDuration.text = song.songDurationString

        invalidateIsFavorite()
    }

    override val layoutResId: Int = R.layout.fragment_nowplaying

    override fun onDestroyView() {
        super.onDestroyView()
        activity.unregisterReceiver(volumeChangeListener)
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        val song = PlaybackRemote.getCurrentSong() ?: return false
        when (item?.itemId) {
            R.id.item_add_to_favorites -> {
                toggleIsFavorite()
                return true
            }
            R.id.item_show_artist -> {
                startActivity(Intent(activity, ArtistActivity::class.java).putExtra("artist_id", song.songArtistId), ActivityOptionsCompat.makeSceneTransitionAnimation(activity).toBundle())
                return true
            }
            R.id.item_show_album -> {
                startActivity(Intent(activity, AlbumActivity::class.java).putExtra("album_id", song.songAlbumId), ActivityOptionsCompat.makeSceneTransitionAnimation(activity).toBundle())
                return true
            }
            R.id.item_share -> {
                startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).setType("audio/*").putExtra(Intent.EXTRA_STREAM, song.uri), getString(R.string.share_this_track)))
                return true
            }
            else -> return false
        }
    }

    private fun invalidateIsFavorite() {
        async {
            toolbar.menu.findItem(R.id.item_add_to_favorites).setIcon(if (await {
                PlaybackRemote.getCurrentSong()!!.isFavorite(activity)
            }) R.drawable.ic_favorite_white_24dp else R.drawable.ic_favorite_border_white_24dp)
        }
    }

    private fun toggleIsFavorite() = async {
        val song = PlaybackRemote.getCurrentSong()!!
        val isFavorite = await { song.isFavorite(activity) }
        val finalValue = await { song.setFavorite(activity, !isFavorite) }
        toolbar.menu.findItem(R.id.item_add_to_favorites).icon = ContextCompat.getDrawable(activity, if (finalValue) R.drawable.ic_favorite_white_24dp else R.drawable.ic_favorite_border_white_24dp)
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
        if (seekBar == seekBars[0]) PlaybackRemote.seekTo(seekBar.progress)
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        if (fromUser && seekBar == seekBars[1]) audioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PlaybackRemote.registerCallback(this)
    }

    override fun onStart() {
        super.onStart()
        if (PlaybackRemote.isActive()) PlaybackRemote.requestUpdates(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        PlaybackRemote.unregisterCallback(this)
    }

    val bufferDialog: MaterialDialog by lazy {
        MaterialDialog.Builder(activity)
                .title("Buffering")
                .content("Buffering... \nIf this process takes too long, dismiss this dialog")
                .progress(true, 0)
                .cancelable(false)
                .positiveText(R.string.dismiss)
                .onPositive { dialog, which ->
                    dialog.setCancelable(true)
                    dialog.dismiss()
                }
                .build()
    }

    override fun onBufferStarted() {
        if ((PlaybackRemote.getCurrentSong()?.getData("is_radio") as Boolean?) ?: false) bufferDialog.show()
    }

    override fun onBufferFinished() {
        if (bufferDialog.isShowing) bufferDialog.dismiss()
    }

    inner class VolumeChangeListener : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            seekBars[1].progress = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: 0
        }

    }

}