package com.crossbowffs.syncshot.activity

import android.app.ProgressDialog
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import com.crossbowffs.syncshot.MyApp
import com.crossbowffs.syncshot.R
import com.crossbowffs.syncshot.model.UserData
import com.google.common.util.concurrent.FutureCallback
import com.google.common.util.concurrent.Futures
import com.microsoft.windowsazure.mobileservices.MobileServiceClient
import com.microsoft.windowsazure.mobileservices.authentication.MobileServiceAuthenticationProvider
import com.microsoft.windowsazure.mobileservices.authentication.MobileServiceUser
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.toolbar.*

class LoginActivity : AppCompatActivity() {
    private val REQUEST_SETUP_USER = 3

    private lateinit var serviceClient: MobileServiceClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        setSupportActionBar(toolbar)

        serviceClient = (application as MyApp).serviceClient

        loginButton.setOnClickListener {
            // Ugly hack because the Azure library blindly casts the context to activity
            // Luckily that's only done here, so we just swizzle the context field for one call
            val contextField = serviceClient.javaClass.getDeclaredField("mContext")
            contextField.isAccessible = true
            contextField.set(serviceClient, this)
            val token = serviceClient.login(MobileServiceAuthenticationProvider.Twitter)
            contextField.set(serviceClient, application)
            Futures.addCallback(token, object : FutureCallback<MobileServiceUser> {
                override fun onFailure(p0: Throwable?) {
                    AlertDialog.Builder(this@LoginActivity)
                        .setTitle("Login failed")
                        .setMessage(p0.toString())
                        .setPositiveButton("Aww!", null)
                        .show()
                }

                override fun onSuccess(p0: MobileServiceUser?) {
                    setupNewUserIfNecesary(p0!!.userId)
                }
            });
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_SETUP_USER) {
            setResult(resultCode)
            finish()
        }
    }

    private fun setupNewUserIfNecesary(userId: String) {
        val dialog = ProgressDialog(this)
        val task = object : AsyncTask<Void, Void, Boolean>() {
            override fun doInBackground(vararg p0: Void?): Boolean {
                val userTable = serviceClient.getTable(UserData::class.java)
                val matchUsers = userTable.where().field("id").eq(userId).execute().get()
                return (matchUsers.size != 0)
            }

            override fun onPostExecute(result: Boolean) {
                dialog.dismiss()
                if (result) {
                    setResult(RESULT_OK)
                    finish()
                } else {
                    val intent = Intent(this@LoginActivity, SetupUserActivity::class.java)
                    startActivityForResult(intent, REQUEST_SETUP_USER)
                }
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
