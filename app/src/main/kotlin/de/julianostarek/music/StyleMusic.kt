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

package de.julianostarek.music

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Process
import android.support.v4.media.session.PlaybackStateCompat
import com.afollestad.materialdialogs.MaterialDialog
import com.github.javiersantos.piracychecker.PiracyChecker
import com.github.javiersantos.piracychecker.PiracyCheckerUtils
import com.github.javiersantos.piracychecker.enums.InstallerID
import com.github.javiersantos.piracychecker.enums.PiracyCheckerCallback
import com.github.javiersantos.piracychecker.enums.PiracyCheckerError
import com.google.firebase.FirebaseApp
import de.julianostarek.music.databases.MetadataHelper
import de.julianostarek.music.extensions.addPlayed
import de.julianostarek.music.extensions.getBaseDirectory
import de.julianostarek.music.helper.AppColors
import de.julianostarek.music.helper.PreferenceConstants
import de.julianostarek.music.lastfm.ArtistMetadataService
import mobile.substance.sdk.colors.ColorPackage
import mobile.substance.sdk.music.core.objects.Artist
import mobile.substance.sdk.music.core.objects.Song
import mobile.substance.sdk.music.loading.Library
import mobile.substance.sdk.music.loading.tasks.MediaLoader
import mobile.substance.sdk.music.playback.PlaybackRemote
import mobile.substance.sdk.options.DynamicColorsOptions
import mobile.substance.sdk.options.MusicCoreOptions
import mobile.substance.sdk.options.MusicPlaybackOptions
import org.jetbrains.anko.defaultSharedPreferences
import java.io.File
import java.io.IOException
import kotlin.concurrent.thread

class StyleMusic : Application(), PlaybackRemote.RemoteCallback, MediaLoader.TaskListener<Artist> {
    private var hasCheckedSuccessfully = false

    inner class LicenseCallback(private val activity: Activity) : PiracyCheckerCallback() {

        override fun allow() {
            hasCheckedSuccessfully = true
        }

        override fun dontAllow(p0: PiracyCheckerError?) {
            MaterialDialog.Builder(activity)
                    .title("License verification")
                    .content("Trying to crack this app? You're not good enough!")
                    .cancelable(false)
                    .showListener {
                        Handler().postDelayed({
                            System.exit(0)
                        }, 10000)
                    }.show()
        }

        override fun onError(error: PiracyCheckerError?) {
            println(error)
        }

    }

    override fun onCompleted(result: List<Artist>) {
        thread {
            if (defaultSharedPreferences.getBoolean(PreferenceConstants.PREF_KEY_ARTIST_METADATA, false)) {
                result.filter { it.artistName != "<unknown>" }
                        .forEach {
                            if (!MetadataHelper.contains(this, it.id, Artist::class.java.simpleName.toLowerCase() + "_image")) {
                                startService(Intent(this, ArtistMetadataService::class.java)
                                        .putExtra("artist_name", it.artistName)
                                        .putExtra("artist_id", it.id))
                            } else it.artistImagePath = MetadataHelper.getPath(this, it.id, Artist::class.java.simpleName.toLowerCase() + "_image")
                        }
            }
        }
    }

    override fun onOneLoaded(item: Artist, pos: Int) {

    }

    override fun onDurationChanged(duration: Int, durationString: String) {
    }

    override fun onProgressChanged(progress: Int) {
    }

    override fun onQueueChanged(queue: List<Song>) {
    }

    override fun onRepeatModeChanged(@PlaybackStateCompat.RepeatMode mode: Int) {
    }

    override fun onSongChanged(song: Song) {
        if (song.getData("is_radio") == null) song.addPlayed(this)
    }

    override fun onStateChanged(state: Int) {
    }

    override fun onError() {

    }

    override fun onCreate() {
        super.onCreate()
        if (!BuildConfig.DEBUG) performPublicChecks()
        FirebaseApp.initializeApp(this)
        init()
        assureDirectory()
    }

    private fun performPublicChecks() {
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityPaused(activity: Activity?) {

            }

            override fun onActivityResumed(activity: Activity?) {
                if (BuildConfig.BUILD_TYPE == "preview") performDeadlineCheck(activity!!)
            }

            override fun onActivityStarted(activity: Activity?) {

            }

            override fun onActivityDestroyed(activity: Activity?) {

            }

            override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {

            }

            override fun onActivityStopped(activity: Activity?) {

            }

            override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {
                if (BuildConfig.BUILD_TYPE == "release" && !hasCheckedSuccessfully) performLicenseCheck(activity!!)
            }

        })
    }

    private fun performDeadlineCheck(activity: Activity) {
        try {
            val deadline = BuildConfig::class.java.getDeclaredField("DEADLINE").getDouble(null)
            if (deadline < System.currentTimeMillis()) {
                MaterialDialog.Builder(activity)
                        .title("Deprecation")
                        .content("This preview version of Style Music is meant to be used only one week starting from the time it was built. Get the latest alpha or use the release version. The app will close in 10 seconds")
                        .cancelable(false)
                        .showListener {
                            Handler().postDelayed({
                                System.exit(0)
                            }, 10000)
                        }
                        .show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun performLicenseCheck(activity: Activity) {
        val licensingKey = BuildConfig::class.java.getDeclaredField("GOOGLE_PLAY_LICENSING_KEY").get(null) as String
        PiracyChecker(activity)
                .enableSigningCertificate(PiracyCheckerUtils.getAPKSignature(this))
                .enableInstallerId(InstallerID.GOOGLE_PLAY)
                .enableGooglePlayLicensing(licensingKey)
                .callback(LicenseCallback(activity))
                .start()
    }

    private fun init() {
        MusicCoreOptions.apply {
            glidePreferPlaceholder = true
            defaultArt = R.drawable.placeholder_album
            defaultArtUrl = "https://style-music.firebaseapp.com/default_artwork.png"
        }
        MusicPlaybackOptions.apply {
            statusbarIconResId = R.drawable.ic_audiotrack_white_24dp
            defaultCallback = this@StyleMusic
            isCastEnabled = true
            castApplicationId = BuildConfig.CAST_ID
            isGaplessPlaybackEnabled = defaultSharedPreferences.getBoolean(PreferenceConstants.PREF_KEY_GAPLESS_PLAYBACK, false)
            isLockscreenArtworkEnabled = defaultSharedPreferences.getBoolean(PreferenceConstants.PREF_KEY_LOCKSCREEN_ALBUM_ART, true)
            isLockscreenArtworkBlurEnabled = defaultSharedPreferences.getBoolean(PreferenceConstants.PREF_KEY_LOCKSCREEN_ALBUM_ART_BLUR, false)
        }
        DynamicColorsOptions.defaultColors = ColorPackage(AppColors.ACCENT_COLOR, AppColors.PRIMARY_COLOR)
        Library.enable()
    }

    override fun onTerminate() {
        super.onTerminate()
        try {
            Library.unregisterArtistListener(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun assureDirectory() = thread {
        val noMedia = File(getBaseDirectory().path + File.separator + ".nomedia")
        if (!noMedia.exists()) {
            noMedia.parentFile.mkdirs()
            try {
                noMedia.createNewFile()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}
