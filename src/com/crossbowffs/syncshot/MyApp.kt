package com.crossbowffs.syncshot

import android.app.Application
import com.microsoft.windowsazure.mobileservices.MobileServiceClient
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.SQLiteLocalStore
import com.microsoft.windowsazure.mobileservices.table.sync.synchandler.SimpleSyncHandler

class MyApp : Application() {
    lateinit var serviceClient: MobileServiceClient
        private set

    override fun onCreate() {
        super.onCreate()
        serviceClient = MobileServiceClient("https://syncshot.azurewebsites.net", this);
        serviceClient.syncContext.initialize(SQLiteLocalStore(this, "SSDB", null, 1), SimpleSyncHandler())
    }
}
