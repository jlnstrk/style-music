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

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.TextInputEditText
import android.support.design.widget.TextInputLayout
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentManager
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import de.julianostarek.music.R
import de.julianostarek.music.databases.RadioHelper
import de.julianostarek.music.helper.RequestCodes
import de.julianostarek.music.types.DatabaseRadioStation
import mobile.substance.sdk.colors.ColorConstants
import mobile.substance.sdk.utils.MusicCoreUtil
import org.jetbrains.anko.find
import org.jetbrains.anko.textResource

class ModifyStationDialog : DialogFragment() {
    lateinit var titleEditText: TextInputEditText
    lateinit var descriptionEditText: TextInputEditText
    lateinit var streamEditText: TextInputEditText
    lateinit var imageTextView: TextView
    var station: DatabaseRadioStation? = null

    companion object {

        fun showWith(station: DatabaseRadioStation?, fragmentManager: FragmentManager): ModifyStationDialog {
            val dialog = ModifyStationDialog()
            dialog.station = station
            dialog.show(fragmentManager, ModifyStationDialog::class.java.simpleName)
            return dialog
        }

    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_modify_station, null, false)

        titleEditText = view.find<TextInputEditText>(R.id.dialog_modify_station_title_text)
        descriptionEditText = view.find<TextInputEditText>(R.id.dialog_modify_station_description_text)
        streamEditText = view.find<TextInputEditText>(R.id.dialog_modify_station_stream_text)
        imageTextView = view.find<TextView>(R.id.dialog_modify_station_image_text)
        imageTextView.setOnClickListener {
            startActivityForResult(Intent(Intent.ACTION_GET_CONTENT).setType("image/*").putExtra(Intent.EXTRA_LOCAL_ONLY, true), RequestCodes.GET_CONTENT_REQUEST_CODE)
        }

        if (station == null) {
            imageTextView.textResource = R.string.add_image
        } else {
            titleEditText.setText(station?.name)
            descriptionEditText.setText(station?.genre)
            if (station?.imagePath != null && station!!.imagePath!!.isNotEmpty()) imageTextView.text = station?.imagePath
            streamEditText.setText(station?.streamUrl)
        }

        val builder = MaterialDialog.Builder(context)
                .title(if (station == null) R.string.insert_station else R.string.modify_station)
                .customView(view, true)
                .positiveText(R.string.save)
                .onPositive { dialog, _ ->
                    if (titleEditText.text.toString().isEmpty()) {
                        ((titleEditText.parent as FrameLayout).parent as TextInputLayout).error = getString(R.string.must_not_be_empty)
                    } else if (descriptionEditText.text.toString().isEmpty()) {
                        ((descriptionEditText.parent as FrameLayout).parent as TextInputLayout).error = getString(R.string.must_not_be_empty)
                    } else if (streamEditText.text.toString().isEmpty()) {
                        ((streamEditText.parent as FrameLayout).parent as TextInputLayout).error = getString(R.string.must_not_be_empty)
                    } else if (station == null) {
                        RadioHelper.insertCustomStation(activity, titleEditText.text.toString(),
                                descriptionEditText.text.toString(),
                                if (imageTextView.text.toString().isEmpty()) null else imageTextView.text.toString(),
                                streamEditText.text.toString())
                        dialog.dismiss()
                    } else {
                        RadioHelper.updateStation(activity, DatabaseRadioStation(titleEditText.text.toString(),
                                descriptionEditText.text.toString(),
                                if (imageTextView.text.toString().isEmpty()) null else imageTextView.text.toString(),
                                streamEditText.text.toString(),
                                station?.dbRowId!!))
                        dialog.dismiss()
                    }
                }
                .autoDismiss(false)
                .negativeText(R.string.cancel)
                .negativeColor(ColorConstants.TEXT_COLOR_SECONDARY_LIGHT_BG)
                .onNegative { dialog, which ->
                    dialog.dismiss()
                }
        if (station != null) {
            builder.neutralText(R.string.delete)
                    .neutralColor(ColorConstants.TEXT_COLOR_SECONDARY_LIGHT_BG)
                    .onNeutral { dialog, which ->
                        dialog.dismiss()
                        RadioHelper.delete(activity, station!!.dbRowId)
                    }
        }
        return builder.build()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RequestCodes.GET_CONTENT_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            imageTextView.text = MusicCoreUtil.getFilePath(activity, data!!.data)
        }
    }

}