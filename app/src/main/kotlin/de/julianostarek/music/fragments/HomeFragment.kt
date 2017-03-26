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
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import de.julianostarek.music.anko.fragments.HomeUI
import mobile.substance.sdk.music.loading.Library
import org.jetbrains.anko.AnkoContext


class HomeFragment : Fragment(), () -> Any {
    lateinit var UI: HomeUI
        private set
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) = UI.recyclerView.adapter.notifyItemChanged(0)
    }

    override fun invoke() = UI.recyclerView.swapAdapter(UI.Adapter(), true)


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        UI = HomeUI()
        return UI.createView(AnkoContext.create(activity, this))
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity.registerReceiver(receiver, IntentFilter("de.julianostarek.music.most_played.CHANGE"))
        Library.registerBuildFinishedListener(this, true)
    }

    override fun onDestroyView() {
        activity.unregisterReceiver(receiver)
        super.onDestroyView()
    }

}

