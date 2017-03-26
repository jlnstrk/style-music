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
import android.speech.RecognizerIntent
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import butterknife.bindView
import de.julianostarek.music.R
import de.julianostarek.music.adapters.SearchAdapter
import de.julianostarek.music.helper.RequestCodes

class SearchActivity : PlaybackRemoteActivity() {
    private val editText: EditText by bindView<EditText>(R.id.activity_search_toolbar_edit_text)
    private val toolbar: Toolbar by bindView<Toolbar>(R.id.activity_search_toolbar)
    private val recyclerView: RecyclerView by bindView<RecyclerView>(R.id.activity_search_recycler_view)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        init()
    }

    private fun init() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.recycledViewPool.setMaxRecycledViews(1, 0)

        val adapter = SearchAdapter(this)
        recyclerView.adapter = adapter
        editText.addTextChangedListener(adapter)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_search, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
            R.id.item_voice_input -> {
                startActivityForResult(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), RequestCodes.SPEECH_RECOGNIZER_REQUEST_CODE)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RequestCodes.SPEECH_RECOGNIZER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            editText.setText(data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.first())
        }
    }

}
