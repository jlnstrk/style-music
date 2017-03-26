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

import android.Manifest
import android.os.Bundle
import android.view.View
import com.heinrichreimersoftware.materialintro.app.IntroActivity
import com.heinrichreimersoftware.materialintro.app.NavigationPolicy
import com.heinrichreimersoftware.materialintro.slide.SimpleSlide
import de.julianostarek.music.R
import de.julianostarek.music.helper.PreferenceConstants
import org.jetbrains.anko.defaultSharedPreferences

class AppIntroActivity : IntroActivity(), NavigationPolicy {

    override fun canGoBackward(p0: Int): Boolean {
        return true
    }

    override fun canGoForward(p0: Int): Boolean {
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        isSkipEnabled = false
        setNavigationPolicy(this)

        addSlide(SimpleSlide.Builder()
                .title(R.string.app_name)
                .description(R.string.welcome)
                .image(R.drawable.ic_launcher_flat)
                .background(R.color.primary)
                .backgroundDark(R.color.primary_dark)
                .build())
        addSlide(SimpleSlide.Builder()
                .title(R.string.storage_permission)
                .description(R.string.storage_permission_reason)
                .image(R.drawable.ic_sd_storage_black_transparent_128dp)
                .background(R.color.primary)
                .backgroundDark(R.color.primary_dark)
                .permissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE))
                .build())
        addSlide(SimpleSlide.Builder()
                .title(R.string.artist_metadata)
                .description(R.string.artist_metadata_sum)
                .image(R.drawable.ic_people_black_transparent_128dp)
                .background(R.color.primary)
                .backgroundDark(R.color.primary_dark)
                .buttonCtaLabel(R.string.enable)
                .buttonCtaClickListener {
                    it.visibility = View.GONE
                    defaultSharedPreferences.edit()
                            .putBoolean(PreferenceConstants.PREF_KEY_ARTIST_METADATA, true)
                            .apply()
                }
                .build())
    }

}
