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

import android.os.Bundle
import android.preference.*
import android.view.View
import com.afollestad.materialdialogs.MaterialDialog
import com.bumptech.glide.Glide
import de.julianostarek.music.R
import de.julianostarek.music.databases.MetadataHelper
import de.julianostarek.music.databases.RadioDatabase
import de.julianostarek.music.extensions.getBaseDirectory
import de.julianostarek.music.helper.PreferenceConstants
import mobile.substance.sdk.music.core.objects.Artist
import mobile.substance.sdk.music.core.objects.Playlist
import org.jetbrains.anko.act
import java.io.File
import kotlin.concurrent.thread

class SettingsFragment : PreferenceFragment() {
    private lateinit var startPagePreference: ListPreference
    private lateinit var artistMetadataPreference: SwitchPreference
    private lateinit var allowedNetworkTypePreference: ListPreference
    private lateinit var imageSizePreference: ListPreference
    private lateinit var wipeArtistsDatabasePreference: Preference
    private lateinit var wipePlaylistsDatabasePreference: Preference
    private lateinit var wipeRadioDatabasePreference: Preference
    private lateinit var lockscreenAlbumArtPreference: SwitchPreference
    private lateinit var lockscreenAlbumArtBlurPreference: CheckBoxPreference
    private lateinit var gaplessPlaybackPreference: CheckBoxPreference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.preferences)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    fun init() {
        startPagePreference = findPreference(PreferenceConstants.PREF_KEY_START_PAGE) as ListPreference
        artistMetadataPreference = findPreference(PreferenceConstants.PREF_KEY_ARTIST_METADATA) as SwitchPreference
        allowedNetworkTypePreference = findPreference(PreferenceConstants.PREF_KEY_ALLOWED_NETWORK_TYPE) as ListPreference
        imageSizePreference = findPreference(PreferenceConstants.PREF_KEY_LASTFM_IMAGE_SIZE) as ListPreference
        wipeArtistsDatabasePreference = findPreference(PreferenceConstants.PREF_KEY_WIPE_ARTISTS_DB)
        wipePlaylistsDatabasePreference = findPreference(PreferenceConstants.PREF_KEY_WIPE_PLAYLISTS_DB)
        wipeRadioDatabasePreference = findPreference(PreferenceConstants.PREF_KEY_WIPE_RADIO_DB)
        lockscreenAlbumArtPreference = findPreference(PreferenceConstants.PREF_KEY_LOCKSCREEN_ALBUM_ART) as SwitchPreference
        lockscreenAlbumArtBlurPreference = findPreference(PreferenceConstants.PREF_KEY_LOCKSCREEN_ALBUM_ART_BLUR) as CheckBoxPreference
        gaplessPlaybackPreference = findPreference(PreferenceConstants.PREF_KEY_GAPLESS_PLAYBACK) as CheckBoxPreference

        allowedNetworkTypePreference.isEnabled = artistMetadataPreference.isChecked
        imageSizePreference.isEnabled = artistMetadataPreference.isChecked
        lockscreenAlbumArtBlurPreference.isEnabled = lockscreenAlbumArtPreference.isChecked

        artistMetadataPreference.setOnPreferenceChangeListener { _, newValue ->
            allowedNetworkTypePreference.isEnabled = newValue as Boolean
            imageSizePreference.isEnabled = newValue as Boolean
            true
        }

        lockscreenAlbumArtPreference.setOnPreferenceChangeListener { _, newValue ->
            lockscreenAlbumArtBlurPreference.isEnabled = newValue as Boolean
            true
        }

        wipeArtistsDatabasePreference.setOnPreferenceClickListener {
            MaterialDialog.Builder(activity)
                    .title(R.string.wipe_artists_db)
                    .content(R.string.wipe_artists_db_sum)
                    .negativeText(R.string.cancel)
                    .autoDismiss(true)
                    .positiveText(R.string.ok)
                    .onPositive { _, _ ->
                        Glide.get(activity).clearMemory()
                        thread {
                            MetadataHelper.deleteAllOfType(activity, Artist::class.java.simpleName.toLowerCase() + "_image")
                            MetadataHelper.deleteAllOfType(activity, Artist::class.java.simpleName.toLowerCase() + "_biography")
                            val folder = File(activity.getBaseDirectory().path + File.separator + "Artists" + File.separator)
                            folder.deleteRecursively()
                            folder.mkdirs()
                            Glide.get(activity).clearDiskCache()
                        }
                    }.show()
            true
        }
        wipePlaylistsDatabasePreference.setOnPreferenceClickListener {
            MaterialDialog.Builder(activity)
                    .title(R.string.wipe_playlist_db)
                    .content(R.string.wipe_playlist_db_sum)
                    .negativeText(R.string.cancel)
                    .autoDismiss(true)
                    .positiveText(R.string.ok)
                    .onPositive { _, _ ->
                        MetadataHelper.deleteAllOfType(activity, Playlist::class.java.simpleName.toLowerCase())
                    }.show()
            true
        }
        wipeRadioDatabasePreference.setOnPreferenceClickListener {
            MaterialDialog.Builder(activity)
                    .title(R.string.wipe_radio_db)
                    .content(R.string.wipe_radio_db_sum)
                    .negativeText(R.string.cancel)
                    .autoDismiss(true)
                    .positiveText(R.string.ok)
                    .onPositive { _, _ ->
                        val folder = File(activity.getBaseDirectory().path + File.separator + "Radio" + File.separator)
                        folder.deleteRecursively()
                        folder.mkdirs()
                        activity.deleteDatabase(RadioDatabase.DATABASE_NAME)
                    }.show()
            true
        }
    }
}