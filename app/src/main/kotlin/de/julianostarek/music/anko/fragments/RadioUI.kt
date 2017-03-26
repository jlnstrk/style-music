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

package de.julianostarek.music.anko.fragments

import android.content.Intent
import android.graphics.Bitmap
import android.speech.RecognizerIntent
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.view.GravityCompat
import android.support.v4.view.ViewCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import co.metalab.asyncawait.async
import com.afollestad.materialdialogs.MaterialDialog
import com.arlib.floatingsearchview.FloatingSearchView
import com.arlib.floatingsearchview.suggestions.SearchSuggestionsAdapter
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.Target
import de.julianostarek.music.R
import de.julianostarek.music.activities.FindStationForSongActivity
import de.julianostarek.music.activities.MainActivity
import de.julianostarek.music.adapters.RadioSearchResultsAdapter
import de.julianostarek.music.adapters.RadioStationsAdapter
import de.julianostarek.music.databases.RadioHelper
import de.julianostarek.music.extensions.getBaseDirectory
import de.julianostarek.music.fragments.ModifyStationDialog
import de.julianostarek.music.fragments.RadioFragment
import de.julianostarek.music.lib.radio.BasicStation
import de.julianostarek.music.lib.radio.ExtendedStation
import de.julianostarek.music.lib.radio.RadioAPI
import de.julianostarek.music.lib.radio.streamUrl
import de.julianostarek.music.recyclerview.itemdecorations.ItemDecorations
import de.julianostarek.music.types.BasicStationSuggestion
import de.julianostarek.music.types.DatabaseRadioStation
import de.julianostarek.music.types.PreInsertRadioStation
import mobile.substance.sdk.music.playback.PlaybackRemote
import org.jetbrains.anko.*
import org.jetbrains.anko.design.coordinatorLayout
import org.jetbrains.anko.design.floatingActionButton
import org.jetbrains.anko.recyclerview.v7.recyclerView
import java.io.File
import java.net.URL
import java.net.URLEncoder
import java.util.*

class RadioUI(private val api: RadioAPI) : AnkoComponent<RadioFragment>,
        FloatingSearchView.OnLeftMenuClickListener,
        FloatingSearchView.OnSearchListener,
        SearchSuggestionsAdapter.OnBindSuggestionCallback,
        FloatingSearchView.OnQueryChangeListener,
        View.OnClickListener,
        FloatingSearchView.OnMenuItemClickListener,
        FloatingSearchView.OnFocusChangeListener {
    lateinit var activity: AppCompatActivity
        private set
    lateinit var coordinatorLayout: CoordinatorLayout
        private set
    lateinit var floatingSearchView: FloatingSearchView
        private set
    lateinit var recyclerView: RecyclerView
        private set
    lateinit var floatingActionButton: FloatingActionButton
        private set

    val progressDialog: MaterialDialog by lazy {
        MaterialDialog.Builder(activity)
                .title(R.string.loading)
                .content(R.string.please_wait)
                .progress(true, 0)
                .cancelable(false)
                .build()
    }

    companion object {
        val SPEECH_RECOGNITION_REQUEST_CODE: Int by lazy {
            Random().nextInt(100)
        }
    }

    override fun createView(ui: AnkoContext<RadioFragment>) = with(ui) {
        activity = ctx as AppCompatActivity
        coordinatorLayout {
            coordinatorLayout = this
            layoutParams = ViewGroup.LayoutParams(matchParent, matchParent)
            recyclerView = recyclerView {
                topPadding = dip(60)
                bottomPadding = dip(56)
                clipToPadding = false
                val gridLayoutManager = GridLayoutManager(activity, 3)
                gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        return if (position == 0) 3 else 1
                    }
                }
                layoutManager = gridLayoutManager
                adapter = RadioStationsAdapter(activity)
                addItemDecoration(ItemDecorations.VerticalGridSpacing(activity, 3, includeEdge = true, startAtPosition = 1))
            }.lparams(matchParent, matchParent)
            floatingSearchView = include<FloatingSearchView>(R.layout.fragment_radio_floating_search_view) {
                inflateOverflowMenu(R.menu.menu_radio)
                setSearchHint(ctx.getString(R.string.search_stations))
                setOnSearchListener(this@RadioUI)
                setOnBindSuggestionCallback(this@RadioUI)
                setOnLeftMenuClickListener(this@RadioUI)
                setOnQueryChangeListener(this@RadioUI)
                attachNavigationDrawerToMenuButton((activity as MainActivity).drawerLayout)
                setOnMenuItemClickListener(this@RadioUI)
                setOnFocusChangeListener(this@RadioUI)
            }.lparams(matchParent, matchParent)
            floatingActionButton = floatingActionButton {
                setOnClickListener(this@RadioUI)
                ViewCompat.setTransitionName(this, ctx.getString(R.string.transition_name_fab))
                size = FloatingActionButton.SIZE_NORMAL
                imageResource = R.drawable.ic_music_note_white_24dp
                translationY = if (PlaybackRemote.isActive()) 0F else dip(56).toFloat()
            }.lparams(wrapContent, wrapContent) {
                gravity = Gravity.BOTTOM or Gravity.END
                bottomMargin = dip(72)
                marginEnd = dip(16)
            }
        }
    }

    override fun onFocusCleared() {
        floatingActionButton.animate().setDuration(200).scaleX(1.0F).scaleY(1.0F).start()
    }

    override fun onFocus() {
        floatingActionButton.animate().setDuration(200).scaleX(0.0F).scaleY(0.0F).start()
    }

    override fun onActionMenuItemSelected(item: MenuItem?) {
        when (item?.itemId) {
            R.id.item_voice_input -> (activity as MainActivity).fragment?.startActivityForResult(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), SPEECH_RECOGNITION_REQUEST_CODE)
            R.id.item_radio_nearby -> async {
                progressDialog.show()
                try {
                    val result = await {
                        val connection = URL("http://ifcfg.me/ip").openConnection()
                        val ip = String(connection.inputStream.readBytes())
                        api.getStationsByGeoIP(ip)
                    }
                    if (result != null && result.isNotEmpty()) showResults(result)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                progressDialog.dismiss()

            }
            R.id.item_custom_station -> ModifyStationDialog.showWith(null, activity.supportFragmentManager)
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            floatingActionButton -> activity.startActivity(Intent(activity, FindStationForSongActivity::class.java), ActivityOptionsCompat.makeSceneTransitionAnimation(activity, floatingActionButton, activity.getString(R.string.transition_name_fab)).toBundle())
        }
    }

    override fun onSearchTextChanged(oldQuery: String?, newQuery: String?) {
        async.cancelAll()

        if (newQuery?.length ?: 0 <= 2) return

        async {
            val result = await { api.searchStation(newQuery!!) }
            val suggestions = ArrayList<BasicStationSuggestion>()
            result?.forEach { suggestions.add(BasicStationSuggestion.Companion.parse(it)) }
            floatingSearchView.swapSuggestions(suggestions)
        }
    }

    override fun onBindSuggestion(suggestionView: View?, leftIcon: ImageView?, textView: TextView?, item: SearchSuggestion?, itemPosition: Int) {
        item as BasicStationSuggestion
        async {
            val result = await { api.getStationDetails(item, false) }
            if (result != null && !result.imageUrl.contains("dar.fm/images/darimg"))
                Glide.with(activity).load(result.imageUrl).placeholder(R.drawable.placeholder_album).crossFade().centerCrop().into(leftIcon)
        }
    }

    override fun onSearchAction(currentQuery: String?) {
        floatingSearchView.apply {
            closeMenu(true)
            clearSearchFocus()
            clearSuggestions()
        }
        if (currentQuery != null && currentQuery.length > 2) {
            async {
                val result = await { api.searchStation(currentQuery) }
                if (result != null) showResults(result)
            }
        }
    }

    override fun onSuggestionClicked(searchSuggestion: SearchSuggestion?) {
        if (floatingSearchView.isSearchBarFocused)
            floatingSearchView.clearSearchFocus()
        saveStation(searchSuggestion as BasicStationSuggestion)
    }

    private fun saveStation(searchSuggestion: BasicStationSuggestion) {
        async {
            val result: ExtendedStation = await { api.getStationDetails(searchSuggestion.id)!! }

            var bitmap: Bitmap? = null
            var destination: File? = null
            try {
                bitmap = await { Glide.with(activity).load(result.imageUrl).asBitmap().into(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL).get() }
                destination = File(activity.getBaseDirectory().path + File.separator + "Radio" + File.separator + URLEncoder.encode(searchSuggestion.name, "UTF-8") + result.imageUrl.substring(result.imageUrl.lastIndexOf(".")))
                if (!destination.exists()) {
                    destination.parentFile.mkdirs()
                    destination.createNewFile()
                }
                await {
                    val stream = destination?.outputStream()
                    bitmap?.compress(Bitmap.CompressFormat.PNG, 100, stream)
                    stream?.flush()
                    stream?.close()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            val existingVersionRowId = await { RadioHelper.contains(activity, searchSuggestion.id) }

            if (existingVersionRowId == -1L) {
                val station = PreInsertRadioStation(searchSuggestion.name, searchSuggestion.genre, searchSuggestion.id, if (bitmap != null) destination?.path.orEmpty() else null)
                await { RadioHelper.insertDarFmStation(activity, station) }
                Snackbar.make(coordinatorLayout, activity.getString(R.string.station_saved), Snackbar.LENGTH_SHORT).show()
            } else {
                val station = DatabaseRadioStation(searchSuggestion.name, searchSuggestion.genre, if (bitmap != null) destination?.path.orEmpty() else null, searchSuggestion.streamUrl(), existingVersionRowId)
                await { RadioHelper.updateStation(activity, station) }
                Snackbar.make(coordinatorLayout, activity.getString(R.string.station_saved), Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    override fun onMenuOpened() {
        try {
            (activity as MainActivity).drawerLayout.openDrawer(GravityCompat.START)
        } catch (ignored: Exception) {
        }
    }

    override fun onMenuClosed() {
    }

    private fun showResults(results: List<BasicStation>) {
        MaterialDialog.Builder(activity)
                .title(R.string.results)
                .adapter(RadioSearchResultsAdapter(results, api, this@RadioUI, activity), LinearLayoutManager(activity))
                .negativeText(R.string.dismiss)
                .show()
    }

}