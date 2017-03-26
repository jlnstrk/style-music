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

import android.animation.ValueAnimator
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import android.widget.LinearLayout
import butterknife.bindView
import butterknife.bindViews
import de.julianostarek.music.R
import de.julianostarek.music.helper.AppColors
import de.julianostarek.music.views.ExpandMoreImageView
import de.psdev.licensesdialog.LicensesDialog
import de.psdev.licensesdialog.licenses.ApacheSoftwareLicense20
import de.psdev.licensesdialog.licenses.MITLicense
import de.psdev.licensesdialog.model.Notice
import de.psdev.licensesdialog.model.Notices
import org.jetbrains.anko.dip

class AboutActivity : AppCompatActivity(), View.OnClickListener {

    companion object {
        const val LINK_APP_WEBSITE = "https://style-music.julianostarek.de"
        const val LINK_APP_GOOGLE_PLUS = "https://plus.google.com/communities/100920291547431158689"
        const val LINK_APP_GITHUB = "https://github.com/julianostarek/music"
        const val LINK_APP_GOOGLE_PLAY = "https://play.google.com/store/apps/details?id=de.julianostarek.music"

        const val LINK_SECTION_DEVELOPMENT_GOOGLE_PLUS = "https://plus.google.com/+JulianOstarek"
        const val LINK_SECTION_DEVELOPMENT_GITHUB = "https://github.com/julianostarek"
        const val LINK_SECTION_DESIGN_GOOGLE_PLUS = "https://plus.google.com/+GaigzeanDesigner"
        const val LINK_SECTION_PR_GOOGLE_PLUS = "https://plus.google.com/111376656179530028594"
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.activity_about_link_website -> performIntent(LINK_APP_WEBSITE)
            R.id.activity_about_link_community -> performIntent(LINK_APP_GOOGLE_PLUS)
            R.id.activity_about_link_github -> performIntent(LINK_APP_GITHUB)
            R.id.activity_about_link_google_play -> performIntent(LINK_APP_GOOGLE_PLAY)
            R.id.activity_about_link_development_google_plus -> performIntent(LINK_SECTION_DEVELOPMENT_GOOGLE_PLUS)
            R.id.activity_about_link_development_github -> performIntent(LINK_SECTION_DEVELOPMENT_GITHUB)
            R.id.activity_about_link_design_google_plus -> performIntent(LINK_SECTION_DESIGN_GOOGLE_PLUS)
            R.id.activity_about_link_pr_google_plus -> performIntent(LINK_SECTION_PR_GOOGLE_PLUS)
            R.id.activity_about_section_development_expand_more, R.id.activity_about_section_design_expand_more, R.id.activity_about_section_pr_expand_more -> {
                animateHeight(v.parent as View, (v as ExpandMoreImageView).isExpanded)
                v.toggle()
            }
        }
    }

    private fun animateHeight(view: View, reverse: Boolean = false) {
        val anim = ValueAnimator.ofInt(dip(if (reverse) 132 else 84), dip(if (reverse) 84 else 132))
        anim.interpolator = AccelerateDecelerateInterpolator()
        anim.addUpdateListener {
            (view.layoutParams as LinearLayout.LayoutParams).height = it.animatedValue as Int
            view.requestLayout()
        }
        anim.start()
    }

    private fun performIntent(url: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        startActivity(intent)
    }

    val toolbar: Toolbar by bindView<Toolbar>(R.id.activity_about_toolbar)
    val appLinks: List<Button> by bindViews<Button>(R.id.activity_about_link_website,
            R.id.activity_about_link_community,
            R.id.activity_about_link_github,
            R.id.activity_about_link_google_play)
    val creatorsLinks: List<Button> by bindViews<Button>(R.id.activity_about_link_development_google_plus,
            R.id.activity_about_link_development_github,
            R.id.activity_about_link_design_google_plus,
            R.id.activity_about_link_pr_google_plus)
    val expandMoreImageViews: List<ExpandMoreImageView> by bindViews<ExpandMoreImageView>(R.id.activity_about_section_development_expand_more,
            R.id.activity_about_section_design_expand_more,
            R.id.activity_about_section_pr_expand_more)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        window.statusBarColor = AppColors.PRIMARY_LIGHT_BACKGROUND
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        initClickListeners()
    }

    private fun initClickListeners() {
        appLinks.forEach { it.setOnClickListener(this) }
        creatorsLinks.forEach { it.setOnClickListener(this) }
        expandMoreImageViews.forEach { it.setOnClickListener(this) }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
            R.id.item_licenses -> {
                val notices = Notices()
                notices.addNotice(Notice("Substance SDK", "https://github.com/SubstanceMobile/SDK", "Copyright 2016 Substance Mobile", ApacheSoftwareLicense20()))
                notices.addNotice(Notice("Android Support Libraries", "http://developer.android.com/tools/support-library/index.html", "Copyright (C) 2016 The Android Open Source Project", ApacheSoftwareLicense20()))
                notices.addNotice(Notice("AsyncAwait", "https://github.com/metalabdesign/AsyncAwait", "Copyright 2017 MetaLab", ApacheSoftwareLicense20()))
                notices.addNotice(Notice("PiracyChecker", "https://github.com/javiersantos/PiracyChecker", "Copyright 2016 Javier Santos", ApacheSoftwareLicense20()))
                notices.addNotice(Notice("KotterKnife", "https://github.com/JakeWharton/kotterknife", "Copyright 2014 Jake Wharton", ApacheSoftwareLicense20()))
                notices.addNotice(Notice("Assent", "https://github.com/afollestad/assent", "Copyright 2016 Aidan Follestad", ApacheSoftwareLicense20()))
                notices.addNotice(Notice("material-dialogs", "https://github.com/afollestad/material-dialogs", "Copyright (c) 2014-2016 Aidan Micheal Follestad", MITLicense()))
                notices.addNotice(Notice("Slice", "https://github.com/mthli/Slice", "Copyright (C) 2016 Matthew Lee\nCopyright (C) 2014 The Android Open Source Project", ApacheSoftwareLicense20()))
                notices.addNotice(Notice("RecyclerView-FastScroll", "https://github.com/timusus/RecyclerView-FastScroll", "Copyright (c) 2016, Tim Malseed\nCopyright (C) 2010 The Android Open Source Project", ApacheSoftwareLicense20()))
                notices.addNotice(Notice("FloatingSearchView", "https://github.com/arimorty/floatingsearchview", null, ApacheSoftwareLicense20()))
                notices.addNotice(Notice("material-intro", "https://github.com/HeinrichReimer/material-intro", "Copyright 2016 Heinrich Reimer", ApacheSoftwareLicense20()))


                LicensesDialog.Builder(this)
                        .setTitle(R.string.licenses)
                        .setCloseText(R.string.cancel)
                        .setIncludeOwnLicense(true)
                        .setShowFullLicenseText(false)
                        .setNotices(notices)
                        .build()
                        .showAppCompat()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_about, menu)
        return super.onCreateOptionsMenu(menu)
    }

}
