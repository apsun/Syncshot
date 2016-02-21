package com.crossbowffs.syncshot.adapter

import android.content.Context
import android.view.View
import android.widget.TextView
import com.crossbowffs.syncshot.R
import com.crossbowffs.syncshot.model.EventData
import com.crossbowffs.syncshot.util.unixTimeToDate
import com.crossbowffs.syncshot.util.unixTimeToTime

class EventArrayAdapter(context: Context) : CustomArrayAdapter<EventData>(context, R.layout.listitem_event) {
    override fun bindView(view: View, item: EventData) {
        val nameLabel = view.findViewById(R.id.eventNameLabel) as TextView
        val dtLabel = view.findViewById(R.id.eventDateTimeLabel) as TextView

        nameLabel.text = "${item.name} @ ${item.location}"
        dtLabel.text = "${unixTimeToDate(item.startTime)}, ${unixTimeToTime(item.startTime)} ~ ${unixTimeToTime(item.endTime)}"
    }
}
