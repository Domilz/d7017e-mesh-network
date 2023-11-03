package com.epiroc.wifiaware.transport.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.aware.DiscoverySession
import android.net.wifi.aware.PeerHandle
import android.net.wifi.aware.WifiAwareNetworkSpecifier
import android.net.wifi.aware.WifiAwareSession
import android.util.Log
import com.epiroc.wifiaware.transport.utility.WifiAwareUtility
import java.io.IOException
import java.net.ServerSocket
import java.net.Socket

class PublisherNetwork {
    private var serverSocket: ServerSocket? = null
    private lateinit var networkCallbackPub: ConnectivityManager.NetworkCallback
    private var clientSocket: Socket? = null
    private val messagesReceived: MutableList<String> = mutableListOf()
    private val utility: WifiAwareUtility = WifiAwareUtility
    fun createNetwork(currentPubSession : DiscoverySession, peerHandle : PeerHandle, wifiAwareSession : WifiAwareSession, context : Context){
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

        networkCallbackPub = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                try {
                    serverSocket?.soTimeout = 5000
                    serverSocket?.reuseAddress = true

                    try {
                        clientSocket = serverSocket?.accept()
                    } catch (e: Exception) {
                        serverSocket?.close()
                        return
                    }
                    handleClient(clientSocket)
                    Log.d("1Wifi", "PUBLISH: Accepting client $network")
                } catch (e: Exception) {
                    Log.e("1Wifi", "PUBLISH: ERROR Exception while accepting client", e)
                }
            }

            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                Log.d("1Wifi", "PUBLISH: onCapabilitiesChanged incoming: $networkCapabilities")
            }

            override fun onLost(network: Network) {
                Log.d("1Wifi", "PUBLISH: Connection lost: $network")
                currentPubSession?.close()
                //currentPubSession = null

                try {
                    serverSocket?.close()
                    serverSocket = null
                } catch (e: IOException) {
                    Log.e("1Wifi", "PUBLISH: Error closing the server socket", e)
                }
                Log.e("1Wifi", "PUBLISH: EVERYTHING IN PUBLISH IS NOW CLOSED")
                wifiAwareSession?.close()

            }
        }

        connectivityManager.requestNetwork(myNetworkRequest, networkCallbackPub);
    }

    private fun handleClient(clientSocket: Socket?) {
        Log.d("1Wifi", "PUBLISH: handleClient started.")
        clientSocket!!.getInputStream().bufferedReader().use { reader ->
            reader.lineSequence().forEach { line ->
                messagesReceived.add(line)
                Log.d("INFOFROMCLIENT", "Received from client: $line")
            }
        }
        Log.d("1Wifi", "PUBLISH: All information received we are done $messagesReceived")

        utility.sendPostRequest(messagesReceived.toString())

        Log.d("DONEEE", "publisherDone = true")
    }

    fun closeServerSocket() {
        try {
            serverSocket?.close()
            serverSocket = null
        } catch (e: IOException) {
            Log.e("1Wifi", "PUBLISH: Error closing the server socket", e)
        }
    }

}