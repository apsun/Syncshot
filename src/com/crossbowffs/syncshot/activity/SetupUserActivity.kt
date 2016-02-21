package com.crossbowffs.syncshot.activity

import android.app.ProgressDialog
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.crossbowffs.syncshot.MyApp
import com.crossbowffs.syncshot.R
import com.crossbowffs.syncshot.model.UserData
import com.microsoft.windowsazure.mobileservices.MobileServiceClient
import kotlinx.android.synthetic.main.activity_setup_user.*
import kotlinx.android.synthetic.main.toolbar.*

class SetupUserActivity : AppCompatActivity() {
    private lateinit var serviceClient: MobileServiceClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup_user)
        setSupportActionBar(toolbar)

        serviceClient = (application as MyApp).serviceClient

        saveUserInfoButton.setOnClickListener {
            submitUserInfo()
        }
    }

    private fun submitUserInfo() {
        val name = nameTextBox.text.toString()

        val dialog = ProgressDialog(this)
        val task = object : AsyncTask<Void, Void, Void>() {
            override fun doInBackground(vararg p0: Void?): Void? {
                val userTable = serviceClient.getTable(UserData::class.java)
                val data = UserData(serviceClient.currentUser.userId, name)
                userTable.insert(data).get()
                return null
            }

            override fun onPostExecute(result: Void?) {
                dialog.dismiss()
                setResult(RESULT_OK)
                finish()
            }
        }

        dialog.setMessage("Loading account info...")
        dialog.setOnCancelListener { di ->
            task.cancel(true)
        }
        dialog.show()
        task.execute()
    }
}
