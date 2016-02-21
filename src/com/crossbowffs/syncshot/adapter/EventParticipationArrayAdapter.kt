package com.crossbowffs.syncshot.adapter

import android.content.Context
import android.view.View
import android.widget.TextView
import com.crossbowffs.syncshot.R
import com.crossbowffs.syncshot.model.ParticipationData
import com.crossbowffs.syncshot.model.UserData

class EventParticipationArrayAdapter(context: Context, private val map: Map<String, UserData>)
        : CustomArrayAdapter<ParticipationData>(context, R.layout.listitem_event_user) {
    override fun bindView(view: View, item: ParticipationData) {
        val nameLabel = view.findViewById(R.id.profileNameLabel) as TextView
        // val detailsLabel = view.findViewById(R.id.profileDetailsLabel) as TextView

        nameLabel.text = map[item.userId]!!.name
    }
}
