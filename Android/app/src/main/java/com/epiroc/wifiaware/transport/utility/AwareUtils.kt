package com.epiroc.wifiaware.transport.utility

import android.content.Context
import android.util.Log
import com.epiroc.wifiaware.lib.Config
import java.io.DataOutputStream
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

object WifiAwareUtility {
    private val recentlyConnectedDevices = mutableListOf<DeviceConnection>()
    private var tryCount = 0

    fun add(deviceIdentifier: DeviceConnection) {
        if (!recentlyConnectedDevices.contains(deviceIdentifier))
            tryCount = 0
        recentlyConnectedDevices.add(deviceIdentifier)
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

    fun sendPostRequest(data : ByteArray) {
        val url = URL(Config.getConfigData()?.getString("backend_ip"))
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
        connection.doOutput = true

        val outputStream = DataOutputStream(connection.outputStream)
        val c = data.joinToString(separator = ", ", prefix = "[", postfix = "]") { it.toInt().and(0xFF).toString() }
        outputStream.writeBytes(c)
        outputStream.flush()
        outputStream.close()

        val responseCode = connection.responseCode
        println("Response Code: $responseCode")

        connection.inputStream.bufferedReader().use {
            val response = it.readText()
            println("Response: $response")
        }
    }

    fun incrementTryCount(){
        tryCount++
    }

    fun getTryCount(): Int {
        return tryCount
    }

    fun setTryCount(count : Int) {
        tryCount = count
    }
    fun saveToFile(context: Context, information: ByteArray){
        val file = File(context.filesDir, Config.getConfigData()?.getString("local_storage_file_name"))
        writeByteArrayToFile(information,file.path,file)
    }
    private fun writeByteArrayToFile(byteArray: ByteArray, filePath: String, file: File) {
        try {
            FileOutputStream(filePath).use { fileOutputStream ->
                fileOutputStream.write(byteArray)
                Log.d("1Wifi", byteArray.toString())
            }
            println("Successfully wrote byte array to file: $filePath")
        } catch (e: Exception) {
            e.printStackTrace()
            println("Failed to write byte array to file: $filePath")
        }
    }

}

data class DeviceConnection(val deviceIdentifier: String, val timestamp: Long)
