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

import android.graphics.Color
import android.support.v4.view.GravityCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import de.julianostarek.music.R
import de.julianostarek.music.anko.viewholders.UniversalViewHolder
import de.julianostarek.music.helper.AppColors
import de.julianostarek.music.views.ItemCountObservingRecyclerView
import org.jetbrains.anko.*

object ListSection {

    class ViewHolder(itemView: View, ui: UI, recyclerViewHeight: Int, layoutManager: RecyclerView.LayoutManager, var itemDecoration: RecyclerView.ItemDecoration, activity: AppCompatActivity) : UniversalViewHolder<Nothing>(itemView, ui, activity) {

        init {
            ui.recyclerViewHeight = recyclerViewHeight
            ui.recyclerView.setHasFixedSize(true)
            ui.recyclerView.overScrollMode = RecyclerView.OVER_SCROLL_NEVER
            ui.recyclerView.layoutManager = layoutManager
            ui.recyclerView.addItemDecoration(itemDecoration)
        }

        open class UI : UniversalUI() {
            lateinit var button: Button
                protected set
            lateinit var recyclerView: ItemCountObservingRecyclerView
                protected set
            var recyclerViewHeight: Int = 0
                set(value) {
                    recyclerView.layoutParams.height = value
                    (recyclerView.parent as FrameLayout).layoutParams.height = value + recyclerView.context.dip(48)
                    field = value
                }

            override fun createView(ui: AnkoContext<ViewGroup>) = with(ui) {
                frameLayout {
                    backgroundColor = Color.WHITE
                    layoutParams = RecyclerView.LayoutParams(matchParent, wrapContent)

                    frameLayout {
                        title = textView {
                            setTextAppearance(context, R.style.ItemTitleTextAppearance)
                            maxLines = 1
                            ellipsize = TextUtils.TruncateAt.END
                            gravity = Gravity.CENTER_VERTICAL
                            horizontalPadding = dip(16)
                        }.lparams(matchParent, matchParent) {
                            gravity = Gravity.CENTER_VERTICAL
                        }
                        button = styledButton(R.style.Widget_AppCompat_Button_Borderless) {
                            textResource = R.string.more
                            textColor = AppColors.ACCENT_COLOR
                        }.lparams(wrapContent, matchParent) {
                            gravity = Gravity.CENTER_VERTICAL or GravityCompat.END
                        }
                    }.lparams(matchParent, dip(48))
                    recyclerView = emptyViewRecyclerView {
                        isNestedScrollingEnabled = false
                        isScrollContainer = false
                        setHasFixedSize(true)
                    }.lparams(matchParent, wrapContent) {
                        topMargin = dip(48)
                    }
                }
            }

            class EmptySupported : UI(), (Boolean) -> Unit {

                override fun createView(ui: AnkoContext<ViewGroup>) = with(ui) {
                    frameLayout {
                        backgroundColor = Color.WHITE
                        layoutParams = RecyclerView.LayoutParams(matchParent, wrapContent)

                        frameLayout {
                            title = textView {
                                setTextAppearance(context, R.style.ItemTitleTextAppearance)
                                maxLines = 1
                                ellipsize = TextUtils.TruncateAt.END
                                gravity = Gravity.CENTER_VERTICAL
                                horizontalPadding = dip(16)
                            }.lparams(matchParent, matchParent) {
                                gravity = Gravity.CENTER_VERTICAL
                            }
                            button = styledButton(R.style.Widget_AppCompat_Button_Borderless) {
                                textResource = R.string.more
                                textColor = AppColors.ACCENT_COLOR
                            }.lparams(wrapContent, matchParent) {
                                gravity = Gravity.CENTER_VERTICAL or GravityCompat.END
                            }
                        }.lparams(matchParent, dip(48))
                        recyclerView = emptyViewRecyclerView {
                            isNestedScrollingEnabled = false
                            isScrollContainer = false
                            setHasFixedSize(true)
                            callback = this@EmptySupported
                        }.lparams(matchParent, wrapContent) {
                            topMargin = dip(48)
                        }
                        image = imageView {
                            visibility = View.GONE
                            val params = FrameLayout.LayoutParams(matchParent, (recyclerView.context.displayMetrics.widthPixels * 0.388).toInt())
                            params.topMargin = dip(48)
                            layoutParams = params
                        }
                    }
                }

                override fun invoke(empty: Boolean) {
                    if (empty) {
                        image?.visibility = View.VISIBLE
                        image?.imageResource = R.drawable.empty_state_favorites
                        (recyclerView.parent as FrameLayout).layoutParams.height = (recyclerView.context.displayMetrics.widthPixels * 0.388).toInt() + recyclerView.context.dip(48)
                        button.visibility = View.GONE
                    } else {
                        image?.visibility = View.GONE
                        image?.imageResource = 0
                        (recyclerView.parent as FrameLayout).layoutParams.height = recyclerViewHeight + recyclerView.context.dip(48)
                        button.visibility = View.VISIBLE
                    }
                }

            }

        }

    }

    class HeaderViewHolder(itemView: View, ui: UI, activity: AppCompatActivity) : UniversalViewHolder<Nothing>(itemView, ui, activity) {

        class UI : UniversalUI() {
            lateinit var button: Button

            override fun createView(ui: AnkoContext<ViewGroup>) = with(ui) {
                frameLayout {
                    layoutParams = ViewGroup.LayoutParams(matchParent, dip(48))

                    title = textView {
                        setTextAppearance(context, R.style.ItemTitleTextAppearance)
                        maxLines = 1
                        ellipsize = TextUtils.TruncateAt.END
                        gravity = Gravity.CENTER_VERTICAL
                        horizontalPadding = dip(16)
                    }.lparams(matchParent, matchParent) {
                        gravity = Gravity.CENTER_VERTICAL
                    }
                    button = styledButton(R.style.Widget_AppCompat_Button_Borderless) {
                        textResource = R.string.more
                        textColor = AppColors.ACCENT_COLOR
                    }.lparams(wrapContent, matchParent) {
                        gravity = Gravity.CENTER_VERTICAL or GravityCompat.END
                    }
                }
            }

        }

    }

    fun handleBinding(holder: ViewHolder, titleText: String, actionText: String?, onActionClicked: View.OnClickListener?, adapter: RecyclerView.Adapter<*>) {
        (holder.ui as ListSection.ViewHolder.UI).title?.text = titleText

        if (actionText == null && onActionClicked == null) {
            holder.ui.button.visibility = View.GONE
        } else {
            holder.ui.button.text = actionText
            holder.ui.button.setOnClickListener(onActionClicked)
        }
        holder.ui.recyclerView.adapter = adapter
    }

}