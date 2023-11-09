package com.epiroc.wifiaware.lib

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
}