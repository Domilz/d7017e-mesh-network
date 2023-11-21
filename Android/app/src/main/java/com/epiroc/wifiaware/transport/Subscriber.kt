package com.epiroc.wifiaware.transport

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.aware.*
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.epiroc.wifiaware.lib.Config
import com.epiroc.wifiaware.transport.network.ConnectivityManagerHelper
import com.epiroc.wifiaware.transport.utility.WifiAwareUtility
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tag.Client
import java.net.InetSocketAddress
import java.net.Socket
import java.nio.ByteBuffer
import java.util.Timer
import java.util.TimerTask

class Subscriber(
    ctx: Context,
    nanSession: WifiAwareSession,
    client: Client,
    srvcName: String,
    uuid: ByteArray
) {

    private val peerHandleQueue = ArrayDeque<PeerHandle>()
    private var currentPeerHandle: PeerHandle? = null
    private var responseTimer: Timer? = null
    private val RESPONSE_TIMEOUT = 10000L // 10 seconds for example

    private lateinit var clientSocket: Socket
    private val serviceUUID = uuid
    private val serviceName = srvcName
    private val context = ctx
    private val client = client

    private var wifiAwareSession = nanSession
    private var currentSubSession: DiscoverySession? = null

    private lateinit var networkCallbackSub: ConnectivityManager.NetworkCallback
    private lateinit var  currentNetwork : Network

    fun subscribeToWifiAwareSessions() {
        val handler = Handler(Looper.getMainLooper()) // Use the main looper.
        Log.d("1Wifi","SUBSCRIBE: subscribeToWifiAwareSessions called")

        if (wifiAwareSession == null) {
            Log.d("1Wifi","SUBSCRIBE: Wifi Aware session is not available")
            return
        }

        val subscribeConfig = SubscribeConfig.Builder()
            .setServiceName(serviceName)
            .build()

        var discoverySessionCallback = object : DiscoverySessionCallback() {
            override fun onSubscribeStarted(session: SubscribeDiscoverySession) {
                Log.d("1Wifi", "SUBSCRIBE: Subscribe started.")
                currentSubSession = session
            }

            override fun onServiceDiscovered(
                peerHandle: PeerHandle?,
                serviceSpecificInfo: ByteArray?,
                matchFilter: MutableList<ByteArray>?
            ) {
                Log.d("1Wifi", "SUBSCRIBE: Service discovered from peer: $peerHandle")
                super.onServiceDiscovered(peerHandle, serviceSpecificInfo, matchFilter)

                if (peerHandle != null) {
                    synchronized(peerHandleQueue) {
                        peerHandleQueue.add(peerHandle)
                        if (currentPeerHandle == null) {
                            processNextPeerHandle()
                        }
                    }
                } else {
                    Log.e("1Wifi", "SUBSCRIBE: PeerHandle is null")
                }
            }

            override fun onMessageReceived(peerHandle: PeerHandle, message: ByteArray) {
                Log.d("1Wifi", "SUBSCRIBE MSG_RECEIVED: Message received from peer: $peerHandle")
                responseTimer?.cancel()

                synchronized(peerHandleQueue) {
                    if (currentPeerHandle == peerHandle) {
                        CoroutineScope(Dispatchers.IO).launch {
                            createNetwork(peerHandle, wifiAwareSession, context)
                            currentPeerHandle = null
                            if (peerHandleQueue.isNotEmpty()) {
                                processNextPeerHandle()
                            }
                        }
                    }
                }

            }
        }

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Handle permissions here if needed.
        }
        wifiAwareSession!!.subscribe(subscribeConfig, discoverySessionCallback, handler)
    }

    fun createNetwork(peerHandle : PeerHandle, wifiAwareSession : WifiAwareSession, context : Context) {
        val connectivityManager = ConnectivityManagerHelper.getManager(context)

        Log.d("1Wifi", "SUBSCRIBE: Attempting to establish connection with peer: $peerHandle")
        val networkSpecifier = currentSubSession?.let {
            WifiAwareNetworkSpecifier.Builder(it, peerHandle)
                .setPskPassphrase(Config.getConfigData()!!.getString("discoveryPassphrase"))
                .build()
        }
        val myNetworkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI_AWARE)
            .setNetworkSpecifier(networkSpecifier)
            .build()
        Log.d("NETWORKWIFI","SUBSCRIBER: All necessary wifiaware network things created now awaiting callback")

        networkCallbackSub = object : ConnectivityManager.NetworkCallback() {
            //@RequiresApi(Build.VERSION_CODES.R)
            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                Log.d("NETWORKWIFI","SUBSCRIBER: onCapabilitiesChanged")
                Log.d("1Wifi", "SUBSCRIBE: Network capabilities changed for peer: $peerHandle")
                val peerAwareInfo = networkCapabilities.transportInfo as WifiAwareNetworkInfo
                val peerIpv6 = peerAwareInfo.peerIpv6Addr
                val peerPort = peerAwareInfo.port

                Log.d("1Wifi", "peerport is: $peerPort, aware info: $peerAwareInfo")

                try {
                    clientSocket = network.socketFactory.createSocket() // Don't pass the address and port here.
                    //clientSocket.reuseAddress = true
                    Log.d("1Wifi","SUBSCRIBER: TRYING TO CONNECT! to port $peerPort")
                    clientSocket.connect(InetSocketAddress(peerIpv6, peerPort))


                    handleDataExchange(peerHandle, clientSocket,connectivityManager)
                    //alreadyConnected = false
                        //clientSocket.close()

                    //clientSocket.close()

                } catch (e: Exception) {
                    Log.e("1Wifi", "SUBSCRIBE: ERROR SOCKET COULD NOT BE MADE! ${e.message}")
                }
            }

            override fun onAvailable(network: Network) {
                Log.d("1Wifi", "SUBSCRIBE: Network available for peer: $peerHandle")
                currentNetwork = network
            }

            override fun onLost(network: Network) {
               // currentSubSession?.close()
                //currentSubSession = null
               // clientSocket?.close()
                connectivityManager!!.unregisterNetworkCallback(networkCallbackSub)
               // subscribeToWifiAwareSessions()
                Log.d("1Wifi", "SUBSCRIBE: Network lost for peer: $peerHandle, subscriber restarted")
            }
        }
        connectivityManager?.requestNetwork(myNetworkRequest, networkCallbackSub)
    }

    private fun handleDataExchange(peerHandle: PeerHandle, socket: Socket,connectivityManager : ConnectivityManager) {
        Log.d("1Wifi", "SUBSCRIBE: Attempting to send information to: $peerHandle")
        client.insertSingleMockedReading("Client")
        val state = client.state

        socket.getOutputStream().use { outputStream ->
            val size = state.size
            outputStream.write(ByteBuffer.allocate(4).putInt(size).array())
            try{    // Write the protobuf message bytes
                outputStream.write(state)
                outputStream.flush()
                socket.shutdownOutput()
            }catch (e: Exception){
                Log.e("1Wifi", "SUBSCRIBE: Error in handleDataExchange")
            }
            outputStream.close()
            Log.d("DONEEE", "SUBSCRIBE: All information sent we are done")
        }
        clientSocket?.close()
        networkCallbackSub.onLost(currentNetwork)
        processNextPeerHandle()
    }

    private fun sendMessageToPublisher(peerHandle: PeerHandle) {
        CoroutineScope(Dispatchers.IO).launch {
            Log.d("1Wifi", "SEND_MSG: Sending message to publisher with peer handle: $peerHandle")
            currentSubSession?.sendMessage(
                peerHandle,
                0, // Message type (0 for unsolicited)
                serviceUUID,
            )
        }
    }

    private fun startResponseTimer(peerHandle: PeerHandle) {
        responseTimer?.cancel()
        Log.d("1Wifi", "TIMER: Starting response timer for peer handle: $peerHandle")
        responseTimer = Timer().apply {
            schedule(object : TimerTask() {
                override fun run() {
                    Log.d("1Wifi", "TIMER: Response timeout for peer: $peerHandle")
                    synchronized(peerHandleQueue) {
                        currentPeerHandle = null
                        if (peerHandleQueue.isNotEmpty()) {
                            processNextPeerHandle()
                        } else {
                            Log.d("1Wifi", "TIMER: Queue is empty after timeout.")
                        }
                    }
                }
            }, RESPONSE_TIMEOUT)
        }
    }

    private fun processNextPeerHandle() {
        synchronized(peerHandleQueue) {
            currentPeerHandle = peerHandleQueue.removeFirstOrNull()
            Log.d("1Wifi", "PROCESS_NEXT: Processing next peer handle: $currentPeerHandle")
            currentPeerHandle?.let {
                sendMessageToPublisher(it)
                startResponseTimer(it)
            }?: Log.d("1Wifi", "PROCESS_NEXT: No more peer handles to process.")
        }
    }
}