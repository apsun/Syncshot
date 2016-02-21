package com.crossbowffs.syncshot.activity

import android.app.ProgressDialog
import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.crossbowffs.syncshot.MyApp
import com.crossbowffs.syncshot.R
import com.crossbowffs.syncshot.adapter.EventParticipationArrayAdapter
import com.crossbowffs.syncshot.model.EventData
import com.crossbowffs.syncshot.model.ParticipationData
import com.crossbowffs.syncshot.model.UserData
import com.crossbowffs.syncshot.util.getRelevantPhotos
import com.crossbowffs.syncshot.util.uploadImages
import com.microsoft.windowsazure.mobileservices.MobileServiceClient
import kotlinx.android.synthetic.main.activity_event_details.*
import kotlinx.android.synthetic.main.toolbar.*
import java.nio.charset.Charset

class EventDetailsActivity : AppCompatActivity() {
    private lateinit var serviceClient: MobileServiceClient
    private lateinit var eventParticipationListAdapter: EventParticipationArrayAdapter
    private lateinit var nfcAdapter: NfcAdapter
    private lateinit var event: EventData
    private lateinit var eventId: String
    private lateinit var userIdToObjMap: Map<String, UserData>
    private var initialized = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_details)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        event = intent.getParcelableExtra<EventData>("event")
        eventId = event.id!!

        supportActionBar!!.title = event.name
        toolbar.subtitle = "Created by <unknown>"

        eventParticipantsList.setOnItemClickListener { adapterView, view, i, l ->
            val intent = Intent(this, UserEventPhotosActivity::class.java)
            val item = eventParticipationListAdapter.getItem(i)
            intent.putExtra("event", event)
            intent.putExtra("participant", item)
            intent.putExtra("participantUser", userIdToObjMap[item.userId])
            startActivity(intent)
        }

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if (!nfcAdapter.isEnabled || !nfcAdapter.isNdefPushEnabled) {
            // TODO: need to detect when this condition changes while
            // activity is active
            nfcTipLabel.text = "Please enable NFC to share events."
        } else {
            nfcAdapter.setNdefPushMessage(createNdefMessage(eventId), this)
        }

        serviceClient = (application as MyApp).serviceClient
        loadUserIdToObjectMapAsync()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_event_details, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> {
            finish()
            true
        }
        R.id.menu_item_refresh_details -> {
            loadEventPeopleDetailsAsync()
            true
        }
        R.id.menu_item_upload_images -> {
            uploadImagesAsync()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun uploadImagesAsync() {
        val photos = getRelevantPhotos(this@EventDetailsActivity, event.startTime, event.endTime)

        val dialog = ProgressDialog(this)
        dialog.max = photos.size
        dialog.isIndeterminate = false
        dialog.setMessage("Uploading images...")
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)

        val task = object : AsyncTask<Void, Int, ParticipationData>() {
            override fun doInBackground(vararg p0: Void?): ParticipationData {
                return uploadImages(application as MyApp, event, photos) { done, total ->
                    publishProgress(done, total)
                }
            }

            override fun onProgressUpdate(vararg values: Int?) {
                dialog.progress = values[0]!!
            }

            override fun onPostExecute(result: ParticipationData?) {
                dialog.dismiss()
            }
        }

        dialog.setOnCancelListener { di ->
            task.cancel(true)
            finish()
        }
        dialog.show()
        task.execute()
    }

    private fun loadUserIdToObjectMapAsync() {
        // TODO: This is not scalable at all (also a security flaw),
        // but I'm too lazy to find a proper solution for this
        val dialog = ProgressDialog(this)
        val task = object : AsyncTask<Void, Void, Map<String, UserData>>() {
            override fun doInBackground(vararg p0: Void?): Map<String, UserData> {
                val table = serviceClient.getTable(UserData::class.java)
                val userList = table.execute().get()
                return userList.map { it.id!! to it }.toMap()
            }

            override fun onPostExecute(result: Map<String, UserData>) {
                userIdToObjMap = result
                dialog.dismiss()
                toolbar.subtitle = "Created by ${result[event.creatorId]!!.name}"
                eventParticipationListAdapter = EventParticipationArrayAdapter(this@EventDetailsActivity, result)
                eventParticipantsList.adapter = eventParticipationListAdapter
                loadEventPeopleDetailsAsync()
            }
        }

        dialog.setMessage("Loading user info...")
        dialog.setOnCancelListener { di ->
            task.cancel(true)
            if (!initialized) finish()
        }
        dialog.show()
        task.execute()
    }

    private fun loadEventPeopleDetailsAsync() {
        val query = serviceClient.getTable(ParticipationData::class.java).where().field("eventId").eq(eventId)
        val dialog = ProgressDialog(this)
        val task = object : AsyncTask<Void, Void, List<ParticipationData>>() {
            override fun doInBackground(vararg p0: Void?): List<ParticipationData> {
                val syncContext = serviceClient.syncContext
                syncContext.push().get()
                return query.execute().get()
            }

            override fun onPostExecute(result: List<ParticipationData>) {
                eventParticipationListAdapter.replaceAll(result)
                dialog.dismiss()
                initialized = true
            }
        }

        dialog.setMessage("Loading event participants...")
        dialog.setOnCancelListener { di ->
            task.cancel(true)
            if (!initialized) finish()
        }
        dialog.show()
        task.execute()
    }

    private fun createNdefMessage(id: String): NdefMessage {
        val idBytes = id.toByteArray(Charset.forName("utf-8"))
        val mimeRecord = NdefRecord.createMime("text/plain", idBytes)
        val aar = NdefRecord.createApplicationRecord("com.crossbowffs.syncshot")
        return NdefMessage(mimeRecord, aar)
    }
}
