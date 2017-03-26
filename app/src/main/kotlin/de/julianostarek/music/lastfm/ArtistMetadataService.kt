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

package de.julianostarek.music.lastfm

import android.app.DownloadManager
import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Uri
import android.util.Xml
import de.julianostarek.music.BuildConfig
import de.julianostarek.music.databases.MetadataHelper
import de.julianostarek.music.extensions.getBaseDirectory
import de.julianostarek.music.helper.PreferenceConstants
import mobile.substance.sdk.music.core.objects.Artist
import org.jetbrains.anko.defaultSharedPreferences
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.net.URLEncoder
import java.util.*


class ArtistMetadataService : IntentService(ArtistMetadataService::class.java.simpleName) {

    override fun onHandleIntent(intent: Intent) {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (connectivityManager.activeNetworkInfo != null)
            when (defaultSharedPreferences.getString(PreferenceConstants.PREF_KEY_ALLOWED_NETWORK_TYPE, "0").toInt()) {
                0 -> if (connectivityManager.activeNetworkInfo.type == ConnectivityManager.TYPE_WIFI) queryLastfm(intent)
                1 -> if (connectivityManager.activeNetworkInfo.type == ConnectivityManager.TYPE_MOBILE) queryLastfm(intent)
                2 -> if (connectivityManager.activeNetworkInfo.isConnected) queryLastfm(intent)
            }
    }

    private fun queryLastfm(intent: Intent) {
        val artistName = intent.getStringExtra("artist_name")

        val url = URL("https://ws.audioscrobbler.com/2.0/?method=artist.getinfo&artist=${URLEncoder.encode(artistName, "UTF-8")}&api_key=${BuildConfig.LASTFM_KEY}&autocorrect=1&lang=${Locale.getDefault().language}")
        var inputStream: InputStream? = null
        try {
            inputStream = url.openConnection().getInputStream()
            val parser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(inputStream, null)
            parser.nextTag()

            parser.require(XmlPullParser.START_TAG, null, "lfm")
            parser.nextTag()
            parser.require(XmlPullParser.START_TAG, null, "artist")

            var imageParsed = false
            var name: String? = null

            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.eventType != XmlPullParser.START_TAG) continue

                when (parser.name) {
                    "name" -> name = readName(parser)
                    "image" -> if (!imageParsed) {
                        imageParsed = true
                        readImageUrl(parser, name, intent)
                    } else skip(parser)
                    "bio" -> readBiography(parser, intent)
                    else -> skip(parser)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            MetadataHelper.insert(this, intent.getLongExtra("artist_id", 0), "", Artist::class.java.simpleName.toLowerCase() + "_biography")
            MetadataHelper.insert(this, intent.getLongExtra("artist_id", 0), "", Artist::class.java.simpleName.toLowerCase() + "_image")
        } finally {
            inputStream?.close()
        }
    }

    private fun downloadImage(url: String, name: String?, intent: Intent) {
        println("downloadImage($name)")
        if (url.isNotBlank()) {
            val finalUrl = url.replace("34s", getImageSize())
            val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val destination = File(getBaseDirectory().path + File.separator + "Artists" + File.separator + intent.getLongExtra("artist_id", 0).toString() + ".image")
            if (destination.exists()) destination.delete()
            val request = DownloadManager.Request(Uri.parse(finalUrl)).apply {
                setTitle(name)
                setVisibleInDownloadsUi(false)
                setDestinationUri(Uri.fromFile(destination))
            }
            downloadManager.enqueue(request)
            MetadataHelper.insert(this, intent.getLongExtra("artist_id", 0), destination.path, Artist::class.java.simpleName.toLowerCase() + "_image")
        } else MetadataHelper.insert(this, intent.getLongExtra("artist_id", 0), "", Artist::class.java.simpleName.toLowerCase() + "_image")
    }

    private fun downloadBiography(biography: String, intent: Intent) {
        if (biography.isNotBlank()) {
            val destination = File(getBaseDirectory().path + File.separator + "Artists" + File.separator + intent.getLongExtra("artist_id", 0).toShort() + ".biography")
            if (destination.exists()) destination.delete()
            destination.parentFile.mkdirs()
            destination.createNewFile()
            destination.writeText(biography)
            MetadataHelper.insert(this, intent.getLongExtra("artist_id", 0), destination.path, Artist::class.java.simpleName.toLowerCase() + "_biography")
        } else MetadataHelper.insert(this, intent.getLongExtra("artist_id", 0), "", Artist::class.java.simpleName.toLowerCase() + "_biography")
    }

    private fun readName(parser: XmlPullParser): String? {
        var name: String? = null
        parser.require(XmlPullParser.START_TAG, null, "name")
        if (parser.next() == XmlPullParser.TEXT) {
            name = parser.text
            parser.nextTag()
        }
        parser.require(XmlPullParser.END_TAG, null, "name")
        return name
    }

    private fun readImageUrl(parser: XmlPullParser, name: String?, intent: Intent) {
        parser.require(XmlPullParser.START_TAG, null, "image")
        if (parser.next() == XmlPullParser.TEXT) {
            downloadImage(parser.text, name, intent)
            parser.nextTag()
        }
        parser.require(XmlPullParser.END_TAG, null, "image")
    }

    private fun readBiography(parser: XmlPullParser, intent: Intent) {
        parser.require(XmlPullParser.START_TAG, null, "bio")

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) continue

            if (parser.name == "content") {
                parser.require(XmlPullParser.START_TAG, null, "content")

                if (parser.next() == XmlPullParser.TEXT) {
                    downloadBiography(parser.text, intent)
                    parser.nextTag()
                }

                parser.require(XmlPullParser.END_TAG, null, "content")
            } else skip(parser)
        }
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun skip(parser: XmlPullParser) {
        if (parser.eventType != XmlPullParser.START_TAG) {
            throw IllegalStateException()
        }
        var depth = 1
        while (depth != 0) {
            when (parser.next()) {
                XmlPullParser.END_TAG -> depth--
                XmlPullParser.START_TAG -> depth++
            }
        }
    }

    private fun getImageSize(): String {
        when (defaultSharedPreferences.getString(PreferenceConstants.PREF_KEY_LASTFM_IMAGE_SIZE, "4").toInt()) {
            0 -> return "34s"
            1 -> return "64s"
            2 -> return "174s"
            3 -> return "300x300"
            4 -> return "600x600"
            else -> return ""
        }
    }
}







