package com.epiroc.wifiaware.lib

import android.util.Log
import tag.Client
import tag.Tag
import javax.inject.Inject

class Client @Inject constructor() {
    lateinit var tagClient: Client
    private lateinit var clientName : String

    fun setupClient(name: String) {
        Log.d("Client", "SetupClient")
        clientName = name
        if (!::tagClient.isInitialized) {
            tagClient = Tag.getClient().apply {
                setupClient(name, "83.233.46.128:50051")
            }
        }
    }

    fun getClientName() : String {
        if (::clientName.isInitialized) {
            return clientName
        } else {
            throw IllegalStateException("Client not initialized. Something went wrong in MainActivity.")
        }
    }

    fun insertSingleMockedReading(s : String) {
        if (::tagClient.isInitialized) {
            tagClient.insertSingleMockedReading(s)
        } else {
            throw IllegalStateException("Client not initialized. Call setupClient() first.")
        }
    }

    fun updateReadingOfSelf(rpId : String, rssi : Int) {
        if (::tagClient.isInitialized) {
            tagClient.updateReadingofSelf(rpId, rssi)
            Log.d("TagClient", "updatedReadingOfSelf succeeded")
        } else {
            Log.e("TagClient", "updateReadingOfSelf failed: client not initialized")
        }
    }
}