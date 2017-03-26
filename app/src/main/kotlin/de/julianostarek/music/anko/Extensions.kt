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

package de.julianostarek.music.anko

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v7.view.ContextThemeWrapper
import android.view.ViewManager
import android.widget.Button
import android.widget.ImageView
import de.julianostarek.music.views.HorizontalSquareImageView
import de.julianostarek.music.views.ItemCountObservingRecyclerView
import de.julianostarek.music.views.SquareFrameLayout
import de.julianostarek.music.views.VerticalSquareImageView
import org.jetbrains.anko.`$$Anko$Factories$Sdk21View`
import org.jetbrains.anko.custom.ankoView

inline fun ViewManager.squareFrameLayout(theme: Int = 0): SquareFrameLayout = squareFrameLayout(theme) {}
inline fun ViewManager.squareFrameLayout(theme: Int = 0, init: SquareFrameLayout.() -> Unit): SquareFrameLayout = ankoView(::SquareFrameLayout, theme, init)

inline fun ViewManager.verticalSquareImageView(theme: Int = 0): VerticalSquareImageView = verticalSquareImageView(theme) {}
inline fun ViewManager.verticalSquareImageView(theme: Int = 0, init: VerticalSquareImageView.() -> Unit): VerticalSquareImageView = ankoView(::VerticalSquareImageView, theme, init)

inline fun ViewManager.horizontalSquareImageView(theme: Int = 0): HorizontalSquareImageView = horizontalSquareImageView(theme) {}
inline fun ViewManager.horizontalSquareImageView(theme: Int = 0, init: HorizontalSquareImageView.() -> Unit): HorizontalSquareImageView = ankoView(::HorizontalSquareImageView, theme, init)

inline fun ViewManager.constraintLayout(theme: Int = 0): ConstraintLayout = constraintLayout(theme) {}
inline fun ViewManager.constraintLayout(theme: Int = 0, init: ConstraintLayout.() -> Unit): ConstraintLayout = ankoView(::ConstraintLayout, theme, init)

inline fun ViewManager.emptyViewRecyclerView(theme: Int = 0): ItemCountObservingRecyclerView = emptyViewRecyclerView(theme) {}
inline fun ViewManager.emptyViewRecyclerView(theme: Int = 0, init: ItemCountObservingRecyclerView.() -> Unit): ItemCountObservingRecyclerView = ankoView(::ItemCountObservingRecyclerView, theme, init)

inline fun ViewManager.styledButton(styleRes: Int = 0, init: Button.() -> Unit): Button {
    return ankoView({ if (styleRes == 0) Button(it) else Button(ContextThemeWrapper(it, styleRes), null, 0) }, 0) {
        init()
    }
}

inline fun ViewManager.styledButton(styleRes: Int = 0): Button = styledButton(styleRes) {}

inline fun Context.imageView(theme: Int = 0): ImageView = imageView(theme) {}
inline fun Context.imageView(theme: Int = 0, init: ImageView.() -> Unit): ImageView {
    return ankoView(`$$Anko$Factories$Sdk21View`.IMAGE_VIEW, theme) { init() }
}