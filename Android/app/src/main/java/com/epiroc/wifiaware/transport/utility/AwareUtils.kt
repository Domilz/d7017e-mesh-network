package com.epiroc.wifiaware.transport.utility

import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL

object WifiAwareUtility {
    private val recentlyConnectedDevices = mutableListOf<DeviceConnection>()

    fun add(deviceIdentifier: DeviceConnection) {
        recentlyConnectedDevices.add(deviceIdentifier)
    }

    fun remove(deviceIdentifier: DeviceConnection)  {
        recentlyConnectedDevices.remove(deviceIdentifier)
    }

    fun removeIf() : Boolean{
        val currentTime = System.currentTimeMillis()
        val oneMinuteInMillis: Long = 1 * 60 * 1000
        return recentlyConnectedDevices.removeIf { currentTime - it.timestamp > oneMinuteInMillis }
    }

    fun createDeviceConnection(deviceIdentifier: String,timestamp: Long) : DeviceConnection {
        return DeviceConnection(deviceIdentifier,timestamp)
    }

    fun findDevice(deviceIdentifier: String) : DeviceConnection? {
        return recentlyConnectedDevices.find { it.deviceIdentifier == deviceIdentifier }
    }

    fun isNotEmpty() : Boolean {
        return recentlyConnectedDevices.isNotEmpty()
    }

    fun count(): Int {
        return recentlyConnectedDevices.count()
    }

    fun sendPostRequest(data : String) {
        val url = URL("http://83.233.46.128:4242/debuglog")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
        connection.doOutput = true

        val outputStream = DataOutputStream(connection.outputStream)
        outputStream.writeBytes(data)
        outputStream.flush()
        outputStream.close()

        val responseCode = connection.responseCode
        println("Response Code: $responseCode")

        connection.inputStream.bufferedReader().use {
            val response = it.readText()
            println("Response: $response")
        }
    }
}

data class DeviceConnection(val deviceIdentifier: String, val timestamp: Long)
