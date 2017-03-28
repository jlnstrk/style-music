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

import android.animation.*
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.speech.RecognizerIntent
import android.support.design.widget.*
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.support.v4.view.ViewCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.*
import android.transition.TransitionManager
import android.util.TypedValue
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
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
import de.julianostarek.music.extensions.setupWithNavigationDrawer
import de.julianostarek.music.fragments.ModifyStationDialog
import de.julianostarek.music.fragments.RadioFragment
import de.julianostarek.music.helper.AppColors
import de.julianostarek.music.lib.radio.BasicStation
import de.julianostarek.music.lib.radio.ExtendedStation
import de.julianostarek.music.lib.radio.RadioAPI
import de.julianostarek.music.lib.radio.streamUrl
import de.julianostarek.music.recyclerview.itemdecorations.ItemDecorations
import de.julianostarek.music.types.BasicStationSuggestion
import de.julianostarek.music.types.DatabaseRadioStation
import de.julianostarek.music.types.PreInsertRadioStation
import mobile.substance.sdk.colors.ColorConstants
import mobile.substance.sdk.music.playback.PlaybackRemote
import org.jetbrains.anko.*
import org.jetbrains.anko.appcompat.v7.titleResource
import org.jetbrains.anko.appcompat.v7.toolbar
import org.jetbrains.anko.design.appBarLayout
import org.jetbrains.anko.design.collapsingToolbarLayout
import org.jetbrains.anko.design.coordinatorLayout
import org.jetbrains.anko.design.floatingActionButton
import org.jetbrains.anko.recyclerview.v7.recyclerView
import java.io.File
import java.net.URL
import java.net.URLEncoder
import java.util.*

class RadioUI(private val api: RadioAPI) : AnkoComponent<RadioFragment>,
        FloatingSearchView.OnSearchListener,
        SearchSuggestionsAdapter.OnBindSuggestionCallback,
        FloatingSearchView.OnQueryChangeListener,
        View.OnClickListener,
        FloatingSearchView.OnMenuItemClickListener,
        FloatingSearchView.OnFocusChangeListener, AppBarLayout.OnOffsetChangedListener, Toolbar.OnMenuItemClickListener {
    lateinit var activity: AppCompatActivity
        private set
    lateinit var coordinatorLayout: CoordinatorLayout
        private set
    lateinit var appBarlayout: AppBarLayout
        private set
    lateinit var collapsingToolbarLayout: CollapsingToolbarLayout
        private set
    lateinit  var toolbar: Toolbar
        private set
    lateinit var floatingSearchView: FloatingSearchView
        private set
    private val floatingSearchCardView: CardView by lazy {
        val field = FloatingSearchView::class.java.getDeclaredField("mQuerySection")
        field.isAccessible = true
        field.get(floatingSearchView) as CardView
    }
    lateinit var recyclerView: RecyclerView
        private set
    lateinit var floatingActionButton: FloatingActionButton
        private set
    private var isElevated = false
    private var blockMovements = false

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
        val tv = TypedValue()
        ctx.theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)
        val actionBarSize = TypedValue.complexToDimensionPixelSize(tv.data, resources.displayMetrics)
        coordinatorLayout {
            coordinatorLayout = this
            layoutParams = ViewGroup.LayoutParams(matchParent, matchParent)

            appBarlayout = appBarLayout {
                fitsSystemWindows = true
                addOnOffsetChangedListener(this@RadioUI)
                collapsingToolbarLayout = collapsingToolbarLayout {
                    setExpandedTitleMargin(dip(16), dip(24), dip(16), dip(24))
                    setExpandedTitleTextAppearance(R.style.TextAppearance_AppCompat_Display1)
                    setExpandedTitleColor(ColorConstants.TEXT_COLOR_LIGHT_BG)
                    setExpandedTitleTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL))
                    expandedTitleGravity = Gravity.BOTTOM
                    scrimVisibleHeightTrigger = 0
                    toolbar = toolbar {
                        inflateMenu(R.menu.menu_radio)
                        setOnMenuItemClickListener(this@RadioUI)
                        setupWithNavigationDrawer(activity)
                        layoutParams = CollapsingToolbarLayout.LayoutParams(matchParent, actionBarSize).apply {
                            collapseMode = CollapsingToolbarLayout.LayoutParams.COLLAPSE_MODE_PIN
                        }
                        titleResource = R.string.radio
                    }
                }.lparams(matchParent, actionBarSize + dip(56)) {
                    scrollFlags = AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED
                }
                space().lparams(matchParent, dip(56))
            }
            recyclerView = recyclerView {
                bottomPadding = dip(56)
                clipToPadding = false
                val gridLayoutManager = GridLayoutManager(activity, if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) 3 else 4)
                gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        return if (position == 0) gridLayoutManager.spanCount else 1
                    }
                }
                layoutManager = gridLayoutManager
                adapter = RadioStationsAdapter(activity)
                addItemDecoration(ItemDecorations.VerticalGridSpacing(activity, 3, includeEdge = true, startAtPosition = 1))
            }.lparams(matchParent, matchParent) {
                behavior = AppBarLayout.ScrollingViewBehavior()
            }
            floatingSearchView = include<FloatingSearchView>(R.layout.fragment_radio_floating_search_view) {
                inflateOverflowMenu(R.menu.menu_radio_search_bar)
                setSearchHint(ctx.getString(R.string.search_stations))
                setOnSearchListener(this@RadioUI)
                setOnBindSuggestionCallback(this@RadioUI)
                setOnQueryChangeListener(this@RadioUI)
                attachNavigationDrawerToMenuButton((activity as MainActivity).drawerLayout)
                setOnMenuItemClickListener(this@RadioUI)
                setOnFocusChangeListener(this@RadioUI)
                clipToPadding = false
                // This elevation is invisible, it's used to prevent the AppBarLayout from overlaying the SearchView when getting itself elevated
                elevation = dip(8).toFloat()
            }.lparams(matchParent, matchParent)


            // Modify the SearchView's dimming alpha to match 20% black
            val field = FloatingSearchView::class.java.getDeclaredField("BACKGROUND_DRAWABLE_ALPHA_SEARCH_FOCUSED")
            field.isAccessible = true
            field.setInt(floatingSearchView, Math.round(255 * 0.01F))
            floatingSearchCardView.setCardBackgroundColor(AppColors.PRIMARY_LIGHT_BACKGROUND)
            floatingSearchCardView.cardElevation = 0.0F

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
        adjustPositionWithFocus(false)
        val animator = ValueAnimator.ofArgb(Color.WHITE, AppColors.PRIMARY_LIGHT_BACKGROUND)
                .setDuration(200)
        animator.addUpdateListener {
            floatingActionButton.apply {
                scaleX = it!!.animatedFraction
                scaleY = it!!.animatedFraction
            }
            if (!isElevated) {
                floatingSearchCardView.cardElevation = -it!!.animatedFraction * floatingSearchCardView.dip(2)
                floatingSearchCardView.setCardBackgroundColor(it!!.animatedValue as Int)
            }
        }
        animator.start()
    }

    override fun onFocus() {
        adjustPositionWithFocus(true)
        appBarlayout.setExpanded(false, true)
        val animator = ValueAnimator.ofArgb(AppColors.PRIMARY_LIGHT_BACKGROUND, Color.WHITE)
                .setDuration(200)
        animator.addUpdateListener {
            floatingActionButton.apply {
                scaleX = 1.0F - it!!.animatedFraction
                scaleY = 1.0F - it!!.animatedFraction
            }
            if (!isElevated) {
                floatingSearchCardView.cardElevation = it!!.animatedFraction * floatingSearchCardView.dip(2)
                floatingSearchCardView.setCardBackgroundColor(it!!.animatedValue as Int)
            }
        }
        animator.start()
    }

    override fun onActionMenuItemSelected(item: MenuItem?) {
        onMenuItemClick(item)
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.item_voice_input -> {
                (activity as MainActivity).fragment?.startActivityForResult(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), SPEECH_RECOGNITION_REQUEST_CODE)
                return true
            }
            R.id.item_custom_station -> {
                ModifyStationDialog.showWith(null, activity.supportFragmentManager)
                return true
            }
            R.id.item_search -> {
                floatingSearchCardView.performClick()
                return true
            }
            else -> return false
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
            clearQuery()
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

    private fun showResults(results: List<BasicStation>) {
        MaterialDialog.Builder(activity)
                .title(R.string.results)
                .adapter(RadioSearchResultsAdapter(results, api, this@RadioUI, activity), LinearLayoutManager(activity))
                .negativeText(R.string.dismiss)
                .show()
    }

    override fun onOffsetChanged(appBarLayout: AppBarLayout?, verticalOffset: Int) {
        if (!floatingSearchView.isSearchBarFocused && !blockMovements) floatingSearchView.topPadding = appBarLayout!!.bottom - activity.dip(60)
        if (-verticalOffset == appBarLayout!!.totalScrollRange && !isElevated) {
            collapsingToolbarLayout.setScrimsShown(true, true)
            onCollapsedStateChanged(true)
        } else if (-verticalOffset != appBarLayout!!.totalScrollRange && isElevated) {
            collapsingToolbarLayout.setScrimsShown(false, true)
            onCollapsedStateChanged(false)
        }
    }

    fun onCollapsedStateChanged(elevate: Boolean) {
        isElevated = elevate

        val evaluator = ArgbEvaluator()

        ValueAnimator.ofFloat(0.0F, 1.0F).apply {
            duration = collapsingToolbarLayout.scrimAnimationDuration
            addUpdateListener {
                (activity as MainActivity).drawerLayout.setStatusBarBackgroundColor(evaluator.evaluate(it.animatedFraction, if (elevate) AppColors.PRIMARY_LIGHT_BACKGROUND else AppColors.PRIMARY_LIGHT_BACKGROUND_DARK, if (elevate) AppColors.PRIMARY_LIGHT_BACKGROUND_DARK else AppColors.PRIMARY_LIGHT_BACKGROUND) as Int)
                appBarlayout.setBackgroundColor(evaluator.evaluate(it.animatedFraction, if (elevate) Color.WHITE else AppColors.PRIMARY_LIGHT_BACKGROUND, if (elevate) AppColors.PRIMARY_LIGHT_BACKGROUND else Color.WHITE) as Int)
                floatingSearchCardView.setCardBackgroundColor(evaluator.evaluate(it.animatedFraction, if (elevate) AppColors.PRIMARY_LIGHT_BACKGROUND else Color.WHITE, if (elevate) Color.WHITE else AppColors.PRIMARY_LIGHT_BACKGROUND) as Int)
                floatingSearchCardView.cardElevation = (if (elevate) it!!.animatedFraction else 1.0F - it!!.animatedFraction) * activity.dip(2)
            }
            start()
        }

    }

    private fun adjustPositionWithFocus(focus: Boolean) {
        blockMovements = true
        ValueAnimator.ofInt(floatingSearchView.paddingTop, if (focus) activity.dip(6) else appBarlayout.bottom - activity.dip(60)).apply {
            duration = 200
            addUpdateListener {
                floatingSearchView.topPadding = it!!.animatedValue as Int
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    blockMovements = false
                }
            })
            start()
        }
    }

}