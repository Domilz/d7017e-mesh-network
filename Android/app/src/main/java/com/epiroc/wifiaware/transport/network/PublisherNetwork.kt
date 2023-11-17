
package com.epiroc.wifiaware.transport.network
/*
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.aware.DiscoverySession
import android.net.wifi.aware.PeerHandle
import android.net.wifi.aware.WifiAwareNetworkSpecifier
import android.net.wifi.aware.WifiAwareSession
import android.os.PowerManager.WakeLock
import android.util.Log
import com.epiroc.wifiaware.transport.utility.WifiAwareUtility
import tag.Client
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.EOFException
import java.io.IOException
import java.net.ServerSocket
import java.net.Socket
import java.util.Timer
import java.util.TimerTask

class PublisherNetwork(client : Client, wakelock : WakeLock) {
    private var client = client
    private var wakeLock = wakelock
    private var serverSocket: ServerSocket? = null
    private lateinit var networkCallbackPub: ConnectivityManager.NetworkCallback
    private var clientSocket: Socket? = null
    private val messagesReceived: MutableList<String> = mutableListOf()

    private lateinit var context : Context
    fun createNetwork(currentPubSession : DiscoverySession, peerHandle : PeerHandle, wifiAwareSession : WifiAwareSession, context : Context){
        if (!wakeLock.isHeld) {
            wakeLock.acquire()
        }

        this.context = context
        val connectivityManager = ConnectivityManagerHelper.getManager(context)
        if (serverSocket == null || serverSocket!!.isClosed) {
            serverSocket = ServerSocket(0)
        }

        val port = if (serverSocket!!.localPort != -1) serverSocket!!.localPort else 1337

        val networkSpecifier = WifiAwareNetworkSpecifier.Builder(currentPubSession!!, peerHandle)
            .setPskPassphrase("somePassword")
            .setPort(port)
            .build()
        val myNetworkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI_AWARE)
            .setNetworkSpecifier(networkSpecifier)
            .build()
        Log.d("NETWORKWIFI","PUBLISH: All necessary wifiaware network things created now awaiting callback")
        networkCallbackPub = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                try {
                    Log.d("NETWORKWIFI","PUBLISH: onAvailable")
                    serverSocket?.soTimeout = 1000
                    serverSocket?.reuseAddress = true

                    try {
                        clientSocket = serverSocket?.accept()
                    } catch (e: Exception) {
                        serverSocket?.close()
                        try {
                            if (serverSocket == null || serverSocket!!.isClosed) {
                                serverSocket = ServerSocket(0)
                                serverSocket?.soTimeout = 1000
                                serverSocket?.reuseAddress = true
                                clientSocket = serverSocket?.accept()
                            }
                        } catch (e: Exception) {
                            serverSocket?.close()

                            return
                        }

                    }
                    handleClient(clientSocket,connectivityManager)
                    Log.d("1Wifi", "PUBLISH: Accepting client $network")
                } catch (e: Exception) {
                    Log.e("1Wifi", "PUBLISH: ERROR Exception while accepting client", e)
                }
            }

            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                Log.d("NETWORKWIFI","PUBLISH: onCapabilitiesChanged")
                Log.d("1Wifi", "PUBLISH: onCapabilitiesChanged incoming: $networkCapabilities")
            }

            override fun onLost(network: Network) {
                Log.d("1Wifi", "PUBLISH: Connection lost: $network")
                currentPubSession?.close()
                //currentPubSession = null

                closeServerSocket()
                Log.e("1Wifi", "PUBLISH: EVERYTHING IN PUBLISH IS NOW CLOSED")


            }
        }

        connectivityManager.requestNetwork(myNetworkRequest, networkCallbackPub);
    }

    private fun handleClient(clientSocket: Socket?,connectivityManager : ConnectivityManager ) {
        if (!wakeLock.isHeld) {
            wakeLock.acquire()
        }

        Log.d("1Wifi", "PUBLISH: handleClient started.")
        client.insertSingleMockedReading("publish")
        var sdfsdf = client.state
        clientSocket!!.getInputStream().use { inputStream ->
            val dataInputStream = DataInputStream(inputStream)

            try {
                val size = dataInputStream.readInt()
                if (size > 0) {
                    val messageBytes = ByteArray(size)
                    dataInputStream.readFully(messageBytes)
                    try{
                        client.insert(messageBytes)
                    }catch (e: Exception){
                        Log.d("1Wifi", e.message.toString())
                    }

                    Log.d("INFOFROMCLIENT", "Received protobuf message: ${client.getReadableOfSingleState(messageBytes)}")
                } else {
                    Log.d("INFOFROMCLIENT", "End of stream reached or the connection")
                    //return
                }
            } catch (e: EOFException) {
                // End of stream has been reached or the connection was closed
                Log.d("INFOFROMCLIENT", "End of stream reached or the connection was closed.")
            } catch (e: IOException) {
                // Handle I/O error
                Log.e("INFOFROMCLIENT", "I/O error: ${e.message}")
            }
        }
        Log.d("1Wifi", "PUBLISH: All information received we are done $messagesReceived")
        Log.d("DONEEE", "publisherDone = true")
        Log.d("1Wifi", "${client.getReadableOfSingleState(sdfsdf)}" )
        Log.d("1Wifi", "${client.getReadableOfSingleState(client.state)}" )

        utility.saveToFile(context,client.state)
        connectivityManager!!.unregisterNetworkCallback(networkCallbackPub)
    }

    fun closeServerSocket() {
        try {
            serverSocket?.close()
            serverSocket = null
        } catch (e: IOException) {
            Log.e("1Wifi", "PUBLISH: Error closing the server socket", e)
        }
    }
}*/
