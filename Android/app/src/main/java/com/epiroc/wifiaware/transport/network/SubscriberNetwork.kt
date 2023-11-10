package com.epiroc.wifiaware.transport.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.aware.DiscoverySession
import android.net.wifi.aware.PeerHandle
import android.net.wifi.aware.WifiAwareNetworkInfo
import android.net.wifi.aware.WifiAwareNetworkSpecifier
import android.net.wifi.aware.WifiAwareSession
import android.util.Log
import tag.Client
import java.io.IOException
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.net.InetSocketAddress
import java.net.Socket
import java.nio.ByteBuffer
import java.util.Timer
import java.util.TimerTask

class SubscriberNetwork (client : Client) {
    private var client = client
    private lateinit var networkCallbackSub: ConnectivityManager.NetworkCallback
    private lateinit var  subNetwork : Network
    private lateinit var clientSocket: Socket

    fun createNetwork(currentSubSession : DiscoverySession, peerHandle : PeerHandle, wifiAwareSession : WifiAwareSession, context : Context) {
        val connectivityManager = ConnectivityManagerHelper.getManager(context)
           // temp socket


        Log.d("1Wifi", "SUBSCRIBE: Attempting to establish connection with peer: $peerHandle")
        val networkSpecifier = currentSubSession?.let {
            WifiAwareNetworkSpecifier.Builder(it, peerHandle)
                .setPskPassphrase("somePassword")
                .build()
        }
        val myNetworkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI_AWARE)
            .setNetworkSpecifier(networkSpecifier)
            .build()

        networkCallbackSub = object : ConnectivityManager.NetworkCallback() {
            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                Log.d("1Wifi", "SUBSCRIBE: Network capabilities changed for peer: $peerHandle")
                val peerAwareInfo = networkCapabilities.transportInfo as WifiAwareNetworkInfo
                val peerIpv6 = peerAwareInfo.peerIpv6Addr
                val peerPort = peerAwareInfo.port

                try {
                    clientSocket = network.socketFactory.createSocket() // Don't pass the address and port here.
                    clientSocket.reuseAddress = true
                    clientSocket.connect(InetSocketAddress(peerIpv6, peerPort), 5000)
                    handleDataExchange(peerHandle, clientSocket,connectivityManager)

                } catch (e: Exception) {
                    Log.e("1Wifi", "SUBSCRIBE: ERROR SOCKET COULD NOT BE MADE! ${e.message}")
                    clientSocket.close()
                    wifiAwareSession!!.close()
                    return
                }

                clientSocket?.close()
            }

            override fun onAvailable(network: Network) {
                Log.d("1Wifi", "SUBSCRIBE: Network available for peer: $peerHandle")
                subNetwork = network
            }

            override fun onLost(network: Network) {

                Log.d("1Wifi", "SUBSCRIBE: Network lost for peer: $peerHandle")
                Log.d("1Wifi", "SUBSCRIBE: SUBSCRIBE CONNECTION LOST")


                // Close the SubscribeDiscoverySession
                currentSubSession?.close()
                closeClientSocket()
                //currentSubSession = null
                wifiAwareSession?.close()
            }
        }
        // Request the network and handle connection in the callback as shown above.
        connectivityManager?.requestNetwork(myNetworkRequest, networkCallbackSub)
    }

    private fun handleDataExchange(peerHandle: PeerHandle, socket: Socket,connectivityManager : ConnectivityManager) {
        Log.d("1Wifi", "SUBSCRIBE: Attempting to send information to: $peerHandle")
        client.insertSingleMockedReading("Client")
        val state = client.state

        socket.getOutputStream().use { outputStream ->

            val size = state.size
            outputStream.write(ByteBuffer.allocate(4).putInt(size).array())

            // Now write the actual protobuf message bytes
            try{
                outputStream.write(state)
                outputStream.flush()
                socket.shutdownOutput()
                //outputStream.close()
            }catch (e: Exception){
                Log.e("1Wifi", "SUBSCRIBE: ERROR HERE!!!!!!!!!!!")
            }

            Log.d("1Wifi", "SUBSCRIBE: All information sent we are done")
        }

        Log.d("DONEEE", "subscriberDone = true")
        //subscriberDone = true
        Timer().schedule(object : TimerTask() {
            override fun run() {
                connectivityManager!!.unregisterNetworkCallback(networkCallbackSub)
            }
        }, 1000) // Delay in milliseconds
    }

    fun closeClientSocket() {
        try {
            clientSocket?.close()
        } catch (e: IOException) {
            Log.e("1Wifi", "PUBLISH: Error closing the server socket", e)
        }
    }

}