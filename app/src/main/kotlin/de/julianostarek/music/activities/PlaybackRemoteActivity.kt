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

import android.support.v4.media.session.PlaybackStateCompat
import android.support.v7.app.AppCompatActivity
import de.julianostarek.music.StyleMusicService
import mobile.substance.sdk.music.core.objects.Song
import mobile.substance.sdk.music.playback.PlaybackRemote

open class PlaybackRemoteActivity : AppCompatActivity(), PlaybackRemote.RemoteCallback {

    override fun onDurationChanged(duration: Int, durationString: String) {

    }

    override fun onProgressChanged(progress: Int) {

    }

    override fun onQueueChanged(queue: List<Song>) {

    }

    override fun onSongChanged(song: Song) {

    }

    override fun onStateChanged(state: Int) {

    }

    override fun onRepeatModeChanged(@PlaybackStateCompat.RepeatMode mode: Int) {

    }

    override fun onError() {

    }

    override fun onStart() {
        super.onStart()
        PlaybackRemote.init(StyleMusicService::class.java, this)
        PlaybackRemote.registerCallback(this)
        if (PlaybackRemote.isActive()) PlaybackRemote.requestUpdates(this)
    }

    override fun onStop() {
        super.onStop()
        PlaybackRemote.unregisterCallback(this)
        PlaybackRemote.cleanup()
    }


}