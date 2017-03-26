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

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.design.widget.TextInputEditText
import android.support.design.widget.TextInputLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import butterknife.bindView
import de.julianostarek.music.R
import de.julianostarek.music.databases.MetadataHelper
import de.julianostarek.music.helper.RequestCodes
import mobile.substance.sdk.music.core.objects.Playlist
import mobile.substance.sdk.utils.MusicCoreUtil
import mobile.substance.sdk.utils.MusicTagsUtil

class CreatePlaylistActivity : AppCompatActivity(), View.OnClickListener, Toolbar.OnMenuItemClickListener {
    private val appBarLayout: AppBarLayout by bindView<AppBarLayout>(R.id.activity_create_playlist_app_bar_layout)
    private val toolbar: Toolbar by bindView<Toolbar>(R.id.activity_create_playlist_toolbar)
    private val textInputLayout: TextInputLayout by bindView<TextInputLayout>(R.id.activity_create_playlist_text_input_layout)
    private val textInputEditText: TextInputEditText by bindView<TextInputEditText>(R.id.activity_create_playlist_text_input_edit_text)
    private val pickImageContainer: FrameLayout by bindView<FrameLayout>(R.id.activity_create_playlist_pick_image)
    private val pickImageIcon: ImageView by bindView<ImageView>(R.id.activity_create_playlist_pick_image_icon)
    private val pickImageText: TextView by bindView<TextView>(R.id.activity_create_playlist_pick_image_text)
    private var selectedImagePath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_playlist)
        toolbar.inflateMenu(R.menu.menu_create_playlist)
        toolbar.setOnMenuItemClickListener(this)
        toolbar.setNavigationOnClickListener(this)
        init()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RequestCodes.GET_CONTENT_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            selectedImagePath = MusicCoreUtil.getFilePath(this, data!!.data).orEmpty()
            onImageAdded()
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        if (selectedImagePath != null) outState?.putString("photo_path", selectedImagePath)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        if (savedInstanceState?.containsKey("photo_path") ?: false) {
            selectedImagePath = savedInstanceState?.getString("photo_path")
            onImageAdded()
        }
    }

    private fun onImageAdded() {
        pickImageIcon.setImageResource(R.drawable.ic_check_white_24dp)
        pickImageText.setText(R.string.image_added)
    }

    private fun init() {
        pickImageContainer.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.activity_create_playlist_pick_image -> startActivityForResult(Intent.createChooser(Intent(Intent.ACTION_GET_CONTENT).setType("image/*").putExtra(Intent.EXTRA_LOCAL_ONLY, true), getString(R.string.add_image)), RequestCodes.GET_CONTENT_REQUEST_CODE)
            else -> onBackPressed()
        }
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.item_save -> {
                val newId = MusicTagsUtil.createPlaylist(this, textInputEditText.text.toString())
                if (selectedImagePath != null) MetadataHelper.insert(this, newId, selectedImagePath!!, Playlist::class.java.simpleName.toLowerCase())
                onBackPressed()
                return true
            }
            else -> return false
        }
    }
}
