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

package de.julianostarek.music.extensions

import co.metalab.asyncawait.async
import com.bumptech.glide.Glide
import de.julianostarek.music.R
import de.julianostarek.music.anko.viewholders.*
import de.julianostarek.music.databases.MetadataHelper
import mobile.substance.sdk.colors.ColorConstants
import mobile.substance.sdk.colors.ColorPackage
import mobile.substance.sdk.colors.DynamicColors
import mobile.substance.sdk.colors.DynamicColorsCallback
import mobile.substance.sdk.music.core.dataLinkers.MusicData
import mobile.substance.sdk.music.core.objects.*

fun <T> UniversalViewHolder<T>.bind(item: T, animateIfColorable: Boolean = false) {
    if (item !is MediaObject) return
    if (this is ColorableViewHolder) return this.bindWithColor(item, animateIfColorable)
    if (ui.image != null) Glide.clear(ui.image)

    when (ui) {
        is ImageViewHolders.Horizontal.UI, is ImageViewHolders.Vertical.UI -> {
            when (item) {
                is Album -> item.requestArt(ui.image!!)
                is Artist -> item.requestArt(ui.image!!)
            }
        }
        is MediaItemViewHolders.Small.UI -> {
            when (item) {
                is Song -> {
                    item.requestArt(ui.image!!)
                    ui.icon.setImageResourceTinted(R.drawable.ic_music_note_outline_black_24dp, ColorConstants.ICON_COLOR_ACTIVE_DARK_BG)
                }
                is Album -> {
                    item.requestArt(ui.image!!)
                    ui.icon.setImageResourceTinted(R.drawable.ic_album_black_24dp, ColorConstants.ICON_COLOR_ACTIVE_DARK_BG)
                }
                is Artist -> {
                    item.requestArt(ui.image!!)
                    ui.icon.setImageResourceTinted(R.drawable.ic_person_black_24dp, ColorConstants.ICON_COLOR_ACTIVE_DARK_BG)
                }
            }
        }
        is MediaItemViewHolders.Small.Search.UI -> {
            when (item) {
                is Album -> {
                    ui.title?.text = item.albumName
                    ui.subtitle?.text = item.albumArtistName
                    item.requestArt(ui.image!!)
                }
                is Artist -> {
                    ui.title?.text = item.artistName
                    item.requestArt(ui.image!!)
                }
            }
        }
        is PlaylistViewHolders.Normal.UI -> {
            item as Playlist
            ui.title?.text = item.playlistName
            async {
                val songsCount = await { MusicData.findSongsForPlaylist(item).size }
                // The "quantity" parameter is ignored (idk why) so we just pass the number to the vararg as well
                ui.subtitle?.text = ui.subtitle?.context?.resources?.getQuantityString(R.plurals.numberOfSongs, songsCount, songsCount)
            }
            item.requestArt(ui.image!!)
        }
        is PlaylistViewHolders.Small.UI -> {
            item as Playlist
            ui.title?.text = item.playlistName
        }
        is QueueItemViewHolder.UI -> {
            item as Song
            ui.title?.text = item.songTitle
            ui.subtitle?.text = item.songArtistName
        }
        is SongViewHolders.Normal.UI -> {
            item as Song
            ui.title?.text = item.songTitle
            ui.subtitle?.text = item.songArtistName
            ui.duration.text = item.songDurationString
            item.requestArt(ui.image!!)
        }
        is SongViewHolders.Indexed.UI -> {
            item as Song
            ui.title?.text = item.songTitle
            ui.subtitle?.text = item.songArtistName
            ui.duration.text = item.songDurationString
        }
    }
}

fun <T> ColorableViewHolder<T>.bindWithColor(item: T, animate: Boolean = false) {
    Glide.clear(ui.image)

    when (item) {
        is Song -> {
            ui.title?.text = item.songTitle
            ui.subtitle?.text = item.songArtistName
            item.requestArt(ui.image!!)
        }
        is Album -> {
            ui.title?.text = item.albumName
            ui.subtitle?.text = item.albumArtistName
            item.requestArt(ui.image!!)
        }
        is Artist -> {
            ui.title?.text = item.artistName
            item.requestArt(ui.image!!)
        }
        is Playlist -> {
            ui.title?.text = item.playlistName
            async {
                val songsCount = await { MusicData.findSongsForPlaylist(item).size }
                // The "quantity" parameter is ignored (idk why) so we just pass the number to the vararg as well
                ui.subtitle?.text = ui.subtitle?.context?.resources?.getQuantityString(R.plurals.numberOfSongs, songsCount, songsCount)
            }
            item.requestArt(ui.image!!)
        }
    }

    fun bindWithColor(colors: ColorPackage) {
        (ui as ColorableViewHolder.ColorableUI).colorable.setBackgroundColor(colors.primaryColor)
        when (ui) {
            is SongViewHolders.Favorite.UI -> {
                item as Song
                ui.title?.setTextColor(colors.textColor)
                ui.subtitle?.setTextColor(colors.secondaryTextColor)
            }
            is AlbumViewHolder.UI -> {
                item as Album
                item.requestArt(ui.image!!)
                ui.title?.setTextColor(colors.textColor)
                ui.subtitle?.setTextColor(colors.secondaryTextColor)
            }
            is AlbumViewHolder.AsSong.UI -> {
                item as Song
                ui.title?.setTextColor(colors.textColor)
                ui.subtitle?.setTextColor(colors.secondaryTextColor)
            }
            is ArtistViewHolder.UI -> {
                item as Artist
                ui.title?.setTextColor(colors.textColor)
            }
            is MediaItemViewHolders.Big.UI -> {
                when (item) {
                    is Song -> {
                        ui.title?.setTextColor(colors.textColor)
                        ui.subtitle?.setTextColor(colors.secondaryTextColor)
                        ui.icon.setImageResourceTinted(R.drawable.ic_music_note_outline_black_24dp, colors.iconActiveColor)
                    }
                    is Album -> {
                        ui.title?.setTextColor(colors.textColor)
                        ui.subtitle?.setTextColor(colors.secondaryTextColor)
                        ui.icon.setImageResourceTinted(R.drawable.ic_album_black_24dp, colors.iconActiveColor)
                    }
                    is Artist -> {
                        ui.title?.setTextColor(colors.textColor)
                        ui.icon.setImageResourceTinted(R.drawable.ic_person_black_24dp, colors.iconActiveColor)
                    }
                    is Playlist -> {
                        ui.title?.setTextColor(colors.textColor)
                        ui.subtitle?.setTextColor(colors.secondaryTextColor)
                        ui.icon.setImageResourceTinted(R.drawable.ic_queue_music_black_24dp, colors.iconActiveColor)
                    }
                }
            }
        }
    }

    if ((item as MediaObject).getData("colors") != null) {
        bindWithColor(item.getData("colors") as ColorPackage)
    } else {
        var dynamicColors: DynamicColors? = null
        when (item) {
            is Song -> dynamicColors = DynamicColors.from(MusicData.findAlbumById(item.songAlbumId ?: 0)?.albumArtworkPath.orEmpty())
            is Album -> dynamicColors = DynamicColors.from(item.albumArtworkPath.orEmpty())
            is Artist -> dynamicColors = DynamicColors.from(item.artistImagePath.orEmpty())
            is Playlist -> dynamicColors = DynamicColors.from(MetadataHelper.getPath(ui.image!!.context, item.id, Playlist::class.java.simpleName).orEmpty())
        }
        dynamicColors?.generate(true, object : DynamicColorsCallback {
            override fun onColorsReady(colors: ColorPackage) {
                item.putData("colors", colors)
                bindWithColor(colors)
            }
        })
    }
}