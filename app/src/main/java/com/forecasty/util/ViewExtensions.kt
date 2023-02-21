package com.forecasty.util

import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.bumptech.glide.Glide
import com.forecasty.R

fun ImageView.loadImage(
    @DrawableRes imgId: Int,
    @DrawableRes placeholderRes: Int = R.drawable.ic_img_placeholder
) {
    Glide.with(context)
        .load(imgId)
        .fitCenter()
        .placeholder(placeholderRes)
        .into(this)
}