package com.crossbowffs.syncshot.activity

import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.app.TimePickerDialog
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import com.crossbowffs.syncshot.MyApp
import com.crossbowffs.syncshot.model.EventData
import com.crossbowffs.syncshot.model.ParticipationData
import com.microsoft.windowsazure.mobileservices.MobileServiceClient
import kotlinx.android.synthetic.main.activity_event_create.*
import kotlinx.android.synthetic.main.toolbar.*
import java.util.*

class CreateEventActivity : AppCompatActivity() {
    private lateinit var serviceClient: MobileServiceClient
    private var startTime: Calendar = Calendar.getInstance()
    private var endTime: Calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.crossbowffs.syncshot.R.layout.activity_event_create)
        setSupportActionBar(toolbar);
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        eventChooseDateButton.setOnClickListener { showChooseDateDialog(startTime) }
        eventChooseTimeButton.setOnClickListener { showChooseTimeDialog(startTime) }
        eventChooseEndDateButton.setOnClickListener { showChooseDateDialog(endTime) }
        eventChooseEndTimeButton.setOnClickListener { showChooseTimeDialog(endTime) }
        createEventButtonConfirm.setOnClickListener { createEvent() }
        serviceClient = (application as MyApp).serviceClient
    }

    private fun showChooseDateDialog(field: Calendar) {
        DatePickerDialog(this, DatePickerDialog.OnDateSetListener { datePicker, y, m, d ->
            field.set(y, m, d)
        }, field.get(Calendar.YEAR), field.get(Calendar.MONTH), field.get(Calendar.DATE)).show()
    }

    private fun showChooseTimeDialog(field: Calendar) {
        TimePickerDialog(this, TimePickerDialog.OnTimeSetListener { timePicker, h, m ->
            field.set(Calendar.HOUR_OF_DAY, h)
            field.set(Calendar.MINUTE, m)
        }, field.get(Calendar.HOUR_OF_DAY), field.get(Calendar.MINUTE), false).show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> {
            finish()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun createEvent() {
        val name = eventNameTextBox.text.toString()
        val desc = eventDescriptionTextBox.text.toString()
        val loc = eventLocationTextBox.text.toString()
        val startTimeL = startTime.timeInMillis / 1000
        val endTimeL = endTime.timeInMillis / 1000

        val dialog = ProgressDialog(this)
        val task = object : AsyncTask<Void, Void, Void>() {
            override fun doInBackground(vararg p0: Void?): Void? {
                val userId = serviceClient.currentUser.userId
                val data = EventData(null, userId, name, desc, loc, startTimeL, endTimeL)
                val eventTable = serviceClient.getTable(EventData::class.java)
                val result = eventTable.insert(data).get()
                val parData = ParticipationData(null, userId, result.id!!, 0)
                val parTable = serviceClient.getTable(ParticipationData::class.java)
                parTable.insert(parData).get()
                return null
            }

            override fun onPostExecute(result: Void?) {
                dialog.dismiss()
                setResult(RESULT_OK)
                finish()
            }
        }

        dialog.setMessage("Saving your event, please wait...")
        dialog.setOnCancelListener { di ->
            task.cancel(true)
        }
        dialog.show()
        task.execute()
    }
}
