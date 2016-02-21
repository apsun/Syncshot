package com.crossbowffs.syncshot.adapter

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.crossbowffs.syncshot.R
import com.crossbowffs.syncshot.model.ImageData
import com.crossbowffs.syncshot.util.downloadImage
import com.crossbowffs.syncshot.util.unixTimeToDate
import com.crossbowffs.syncshot.util.unixTimeToTime

class UserEventPhotosAdapter(context: Context) : CustomArrayAdapter<ImageData>(context, R.layout.griditem_event_photo) {
    override fun bindView(view: View, item: ImageData) {
        val imageView = view.findViewById(R.id.photoImage) as ImageView
        val imageLabel = view.findViewById(R.id.photoCaptionLabel) as TextView
        imageLabel.text = "${unixTimeToDate(item.takenTime)} ${unixTimeToTime(item.takenTime)}"

        val imageUrl = downloadImage(item)
        Glide.with(context).load(imageUrl).centerCrop().into(imageView)
    }
}
