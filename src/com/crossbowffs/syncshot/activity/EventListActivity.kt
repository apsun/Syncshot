package com.crossbowffs.syncshot.activity

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.os.AsyncTask
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.crossbowffs.syncshot.MyApp
import com.crossbowffs.syncshot.R
import com.crossbowffs.syncshot.adapter.EventArrayAdapter
import com.crossbowffs.syncshot.model.EventData
import com.crossbowffs.syncshot.model.ParticipationData
import com.microsoft.windowsazure.mobileservices.MobileServiceClient
import kotlinx.android.synthetic.main.activity_event_list.*
import kotlinx.android.synthetic.main.toolbar.*
import java.nio.charset.Charset

class EventListActivity : PrivilegedActivity() {
    private val TAG = EventListActivity::class.java.simpleName

    private val REQUEST_LOGIN = 1
    private val REQUEST_CREATE = 2

    private val REQUEST_STORAGE_READ = 3

    private lateinit var serviceClient: MobileServiceClient
    private lateinit var eventAdapter: EventArrayAdapter
    private var initialized = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_list)
        setSupportActionBar(toolbar);

        eventAdapter = EventArrayAdapter(this)
        eventList.adapter = eventAdapter

        runPrivilegedAction(REQUEST_STORAGE_READ, android.Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, granted: Boolean) {
        if (requestCode == REQUEST_STORAGE_READ) {
            if (!granted) {
                AlertDialog.Builder(this)
                    .setTitle("Permissions required")
                    .setMessage(
                            "While we understand that you don't like giving permissions to random apps, " +
                            "how will we sync your photos if we can't access them?!")
                    .setCancelable(false)
                    .setPositiveButton("OK") { v, i ->
                        finish()
                    }
                    .show()
                return
            }

            createEventButton.setOnClickListener {
                val intent = Intent(this, CreateEventActivity::class.java)
                startActivityForResult(intent, REQUEST_CREATE)
            }

            eventList.setOnItemClickListener { adapterView, view, i, l ->
                val intent = Intent(this, EventDetailsActivity::class.java)
                val item = eventAdapter.getItem(i)
                intent.putExtra("event", item)
                startActivity(intent)
            }

            serviceClient = (application as MyApp).serviceClient
            if (serviceClient.currentUser == null) {
                val intent = Intent(this, LoginActivity::class.java)
                startActivityForResult(intent, REQUEST_LOGIN)
            } else {
                downloadEventListAsync()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_LOGIN) {
            if (resultCode != RESULT_OK) {
                finish()
            } else {
                downloadEventListAsync()
            }
        } else if (requestCode == REQUEST_CREATE) {
            if (resultCode == RESULT_OK) {
                downloadEventListAsync()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action) {
            val msgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
            val msg = msgs[0] as NdefMessage
            val groupId = String(msg.records[0].payload, Charset.forName("utf-8"))
            joinGroupAsync(groupId)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_event_list, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.menu_item_refresh_events -> {
            downloadEventListAsync()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onNewIntent(intent: Intent?) {
        setIntent(intent)
    }

    private fun joinGroupAsync(eventId: String) {
        val dialog = ProgressDialog(this)
        val task = object : AsyncTask<Void, Void, Void>() {
            override fun doInBackground(vararg p0: Void?): Void? {
                val parTable = serviceClient.getTable(ParticipationData::class.java)
                val data = ParticipationData(null, serviceClient.currentUser.userId, eventId, 0)
                parTable.insert(data).get()
                return null
            }

            override fun onPostExecute(result: Void?) {
                dialog.dismiss()
            }
        }
        dialog.setMessage("Joining group $eventId...")
        dialog.setOnCancelListener { di ->
            task.cancel(true)
        }
        dialog.show()
        task.execute()
    }

    private fun downloadEventListAsync() {
        val dialog = ProgressDialog(this)
        val query = serviceClient.getTable(EventData::class.java)
        val task = object : AsyncTask<Void, Void, List<EventData>>() {
            override fun doInBackground(vararg p0: Void?): List<EventData> {
                val syncContext = serviceClient.syncContext;
                syncContext.push().get();
                return query.execute().get()
            }

            override fun onPostExecute(result: List<EventData>) {
                eventAdapter.replaceAll(result)
                dialog.dismiss()
                initialized = true
            }
        }
        dialog.setMessage("Loading event list...")
        dialog.setOnCancelListener { di ->
            task.cancel(true)
            if (!initialized) finish()
        }
        dialog.show()
        task.execute()
    }
}
