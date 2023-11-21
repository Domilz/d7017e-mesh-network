package com.epiroc.wifiaware.lib

import android.util.Log
import tag.Client
import tag.Tag

object Client {
    private lateinit var client: Client // Replace with the actual type of your client

    fun setupClient(clientName: String) : Client? {
        if (!::client.isInitialized) {
            client = Tag.getClient().apply {
                setupClient(clientName)
            }
        }
        return client
    }

    fun insertSingleMockedReading(s : String) {
        if (::client.isInitialized) {
            client.insertSingleMockedReading(s)
        } else {
            throw IllegalStateException("Client not initialized. Call setupClient() first.")
        }
    }

    fun updateReadingOfSelf(rpId : String, rssi : Int) {
        if (::client.isInitialized) {
            client.updateReadingofSelf(rpId, rssi)
            Log.d("TagClient", "updatedReadingOfSelf succeeded")
        } else {
            Log.e("TagClient", "updateReadingOfSelf failed: client not initialized")
        }
    }
}