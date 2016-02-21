package com.crossbowffs.syncshot.activity

import android.app.ProgressDialog
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import com.crossbowffs.syncshot.MyApp
import com.crossbowffs.syncshot.R
import com.crossbowffs.syncshot.adapter.UserEventPhotosAdapter
import com.crossbowffs.syncshot.model.EventData
import com.crossbowffs.syncshot.model.ImageData
import com.crossbowffs.syncshot.model.ParticipationData
import com.crossbowffs.syncshot.model.UserData
import com.microsoft.windowsazure.mobileservices.MobileServiceClient
import kotlinx.android.synthetic.main.activity_event_photos.*
import kotlinx.android.synthetic.main.toolbar.*

class UserEventPhotosActivity : AppCompatActivity() {
    private lateinit var serviceClient: MobileServiceClient
    private lateinit var userEventPhotosAdapter: UserEventPhotosAdapter
    private lateinit var event: EventData
    private lateinit var participantUser: UserData
    private lateinit var participant: ParticipationData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_photos)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        serviceClient = (application as MyApp).serviceClient

        userEventPhotosAdapter = UserEventPhotosAdapter(this)
        photoGrid.adapter = userEventPhotosAdapter

        event = intent.getParcelableExtra<EventData>("event")
        participant = intent.getParcelableExtra<ParticipationData>("participant")
        participantUser = intent.getParcelableExtra<UserData>("participantUser")

        supportActionBar!!.title = event.name
        toolbar.subtitle = "Photos taken by ${participantUser.name}"

        loadImagesForParticipantAsync()
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        android.R.id.home -> {
            finish()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun loadImagesForParticipantAsync() {
        val dialog = ProgressDialog(this)
        val query = serviceClient.getTable(ImageData::class.java).where().field("participantId").eq(participant.id!!)
        val task = object : AsyncTask<Void, Void, List<ImageData>>() {
            override fun doInBackground(vararg p0: Void?): List<ImageData> {
                val syncContext = serviceClient.syncContext;
                syncContext.push().get();
                return query.execute().get()
            }

            override fun onPostExecute(result: List<ImageData>) {
                userEventPhotosAdapter.replaceAll(result)
                dialog.dismiss()
            }
        }
        dialog.setMessage("Loading image list...")
        dialog.setOnCancelListener { di ->
            task.cancel(true)
            finish()
        }
        dialog.show()
        task.execute()
    }
}
