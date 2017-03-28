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

package de.julianostarek.music.recyclerview.itemdecorations


import android.content.Context
import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.util.TypedValue
import android.view.View


sealed class ItemDecorations {

    class HorizontalLinearPaddingLeftSpacing(context: Context, private val spacing: Int = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4F, context.resources.displayMetrics).toInt()) : RecyclerView.ItemDecoration() {

        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State?) {
            outRect.top = spacing
            outRect.bottom = spacing
            outRect.right = spacing
        }
    }

    class HorizontalLinearSpacing(context: Context, private val spacing: Int = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4F, context.resources.displayMetrics).toInt()) : RecyclerView.ItemDecoration() {

        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State?) {
            val position = parent.getChildAdapterPosition(view)
            outRect.top = spacing
            outRect.bottom = spacing
            outRect.right = spacing
            if (position == 0) outRect.left = spacing
        }
    }

    class ThreeItemsStaggeredGrid(context: Context, private val isThirdBig: Boolean = false, private val spacing: Int = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4F, context.resources.displayMetrics).toInt()) : RecyclerView.ItemDecoration() {

        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State?) {
            val position = parent.getChildAdapterPosition(view) // item position

            if (isThirdBig) {
                if (position == 2) {
                    outRect.top = spacing
                    outRect.bottom = spacing
                    outRect.right = spacing
                } else if (position < 2) {
                    outRect.left = spacing
                    outRect.top = if (position == 0) spacing else spacing / 2
                    outRect.bottom = if (position == 0) spacing / 2 else spacing
                } else if (position % 2 == 0) {
                    outRect.top = spacing / 2
                    outRect.bottom = spacing
                } else {
                    outRect.top = spacing
                    outRect.bottom = spacing / 2
                }
            } else {
                if (position == 0) {
                    outRect.left = spacing
                    outRect.top = spacing
                    outRect.bottom = spacing
                } else if (position % 2 == 0) {
                    outRect.top = spacing / 2
                    outRect.bottom = spacing
                } else {
                    outRect.top = spacing
                    outRect.bottom = spacing / 2
                }
            }
            outRect.right = spacing
        }
    }

    class VerticalGridSpacing(context: Context, private val spanCount: Int, private val spacing: Int = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4F, context.resources.displayMetrics).toInt(), private val includeEdge: Boolean, private val startAtPosition: Int = 0) : RecyclerView.ItemDecoration() {

        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State?) {
            val realPosition = parent.getChildAdapterPosition(view) // item position
            if (realPosition < startAtPosition) return
            val position = realPosition - startAtPosition
            val column = position % spanCount // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column + 1) * spacing / spanCount // (column + 1) * ((1f / spanCount) * spacing)

                if (position < spanCount) { // top edge
                    outRect.top = spacing
                }
                outRect.bottom = spacing // item bottom
            } else {
                outRect.left = column * spacing / spanCount // column * ((1f / spanCount) * spacing)
                outRect.right = spacing - (column + 1) * spacing / spanCount // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                if (position >= spanCount) {
                    outRect.top = spacing // item top
                }
            }
        }
    }

}