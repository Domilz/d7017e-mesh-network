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
import com.epiroc.wifiaware.lib.Client
import com.epiroc.wifiaware.lib.Config
import com.epiroc.wifiaware.transport.network.ConnectivityManagerHelper
import com.epiroc.wifiaware.transport.utility.WifiAwareUtility
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketTimeoutException
import java.nio.ByteBuffer
import java.util.Timer
import java.util.TimerTask
import kotlin.math.log

class Subscriber(
    ctx: Context,
    nanSession: WifiAwareSession,
    client: Client,
    srvcName: String
) {

    private val peerHandleQueue = ArrayDeque<PeerHandle>()
    private var currentPeerHandle: PeerHandle? = null
    private var responseTimer: Timer? = null
    private var responseTimer2: Timer? = null
    private val RESPONSE_TIMEOUT = 10000L // 10 seconds for example

    private lateinit var clientSocket: Socket
    private val serviceUUID = client.getClientName().toByteArray()
    private val serviceName = srvcName
    private val context = ctx
    private val client = client

    private var wifiAwareSession = nanSession
    private var currentSubSession: DiscoverySession? = null

    private lateinit var networkCallbackSub: ConnectivityManager.NetworkCallback
    private lateinit var  currentNetwork : Network

    fun subscribeToWifiAwareSessions() {
        val handler = Handler(Looper.getMainLooper()) // Use the main looper.
        Log.d("Subscriber","SUBSCRIBE: subscribeToWifiAwareSessions called")

        if (wifiAwareSession == null) {
            Log.d("Subscriber","SUBSCRIBE: Wifi Aware session is not available")
            return
        }

        val subscribeConfig = SubscribeConfig.Builder()
            .setServiceName(serviceName)
            .build()

        var discoverySessionCallback = object : DiscoverySessionCallback() {
            override fun onSubscribeStarted(session: SubscribeDiscoverySession) {
                Log.d("Subscriber", "SUBSCRIBE: Subscribe started.")
                currentSubSession = session
            }

            override fun onServiceDiscovered(
                peerHandle: PeerHandle?,
                serviceSpecificInfo: ByteArray?,
                matchFilter: MutableList<ByteArray>?
            ) {
                Log.d("Subscriber", "SUBSCRIBE: Service discovered from peer: $peerHandle")
                super.onServiceDiscovered(peerHandle, serviceSpecificInfo, matchFilter)

                if (peerHandle != null) {

                    peerHandleQueue.add(peerHandle)
                    if (peerHandleQueue.size == 1 && currentPeerHandle == null) {
                        Log.d("Subscriber", "NEWPHONE: we are starting for a new phone because peerHandleQueue.size == 1 && currentPeerHandle == null in onServiceDiscovered ${peerHandleQueue.size}")
                        currentPeerHandle = peerHandle
                        processNextPeerHandle()
                    }
                } else {
                    Log.e("Subscriber", "SUBSCRIBE: PeerHandle is null")
                }
            }

            override fun onMessageSendSucceeded(messageId: Int) {
                super.onMessageSendSucceeded(messageId)
                //peerHandleQueue.removeFirstOrNull()
                //Log.e("Subscriber", "SUBSCRIBE: WOOOOHOOOOO onMessageSendSucceeded (this is good) $messageId")
            }

            override fun onMessageSendFailed(messageId: Int) {
                super.onMessageSendFailed(messageId)
                responseTimer?.cancel()
                responseTimer2?.cancel()
                Log.e("Subscriber", "SUBSCRIBE: BUUUUUUUUUUU onMessageSendFailed (this is bad) $messageId")
            }

            override fun onMessageReceived(peerHandle: PeerHandle, message: ByteArray) {
                Log.d("Subscriber", "SUBSCRIBE MSG_RECEIVED: Message received from peer: $peerHandle")
                responseTimer?.cancel()

                synchronized(peerHandleQueue) {
                    if (currentPeerHandle == peerHandle) {
                        CoroutineScope(Dispatchers.IO).launch {
                            createNetwork(peerHandle, context)
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

    fun createNetwork(peerHandle : PeerHandle, context : Context) {
        Log.d("Subscriber", "SUBSCRIBE: Attempting to establish connection with peer: $peerHandle")
        val connectivityManager = ConnectivityManagerHelper.getManager(context)

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
                Log.d("Subscriber", "SUBSCRIBE: Network capabilities changed for peer: $peerHandle")
                super.onCapabilitiesChanged(network, networkCapabilities)

                // Check for Wi-Fi Aware capability
                if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI_AWARE)) {
                    // Handle Wi-Fi Aware capabilities here
                    Log.d("NetworkCallback", "Wi-Fi Aware capabilities changed.")

                    // Example: Check for specific capabilities if needed
                    if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                        // Network has internet capability
                        Log.d("NetworkCallback", "Network has internet capability.")
                    }

                    val wifiAwareNetworkInfo = networkCapabilities.transportInfo as? WifiAwareNetworkInfo
                    wifiAwareNetworkInfo?.let {
                        val peerIpv6Addr = it.peerIpv6Addr
                        val port = it.port

                        var retryCount = 0
                        val maxRetries = 3
                        val retryDelayMillis = 3000L // 5 seconds delay between retries

                        while (retryCount < maxRetries) {
                            try {
                                clientSocket = network.socketFactory.createSocket()
                                clientSocket.soTimeout = 5000 // Set socket timeout (10 seconds for example)

                                Log.d("Subscriber","SUBSCRIBER: TRYING TO CONNECT! to port $port")
                                clientSocket.connect(InetSocketAddress(peerIpv6Addr, port))
                                handleDataExchange(peerHandle, clientSocket, connectivityManager)
                                Log.d("Subscriber","SUBSCRIBER: seems like its done")
                                break // Break the loop if connection is successful
                            } catch (e: SocketTimeoutException) {
                                Log.e("Subscriber", "SUBSCRIBE: Socket timeout occurred. Retrying... (${retryCount + 1})")
                            } catch (e: IOException) {
                                Log.e("Subscriber", "SUBSCRIBE: IO Exception occurred: ${e.message}. Retrying... (${retryCount + 1})")
                            } catch (e: Exception) {
                                Log.e("Subscriber", "SUBSCRIBE: ERROR SOCKET COULD NOT BE MADE! ${e.message}")
                                break // Break on other types of exceptions
                            }

                            retryCount++
                            Thread.sleep(retryDelayMillis) // Wait for some time before retrying
                        }

                        if (retryCount == maxRetries) {
                            Log.e("Subscriber", "SUBSCRIBE: Maximum retry attempts reached. Connection failed.")
                        }
                    }
                }
            }

            override fun onAvailable(network: Network) {
                Log.d("Subscriber", "SUBSCRIBE: Network available for peer: $peerHandle")
                currentNetwork = network
            }

            override fun onLost(network: Network) {
                // currentSubSession?.close()
                //currentSubSession = null
                // clientSocket?.close()
                connectivityManager!!.unregisterNetworkCallback(networkCallbackSub)
                // subscribeToWifiAwareSessions()
                Log.d("Subscriber", "SUBSCRIBE: Network lost for peer: $peerHandle, subscriber restarted")
            }
        }
        connectivityManager?.requestNetwork(myNetworkRequest, networkCallbackSub)
        startResponseTimer2(peerHandle,connectivityManager)
    }

    private fun handleDataExchange(peerHandle: PeerHandle, socket: Socket, connectivityManager: ConnectivityManager) {
        try {
            responseTimer2?.cancel()
            Log.d("Subscriber", "SUBSCRIBE: Attempting to send information to: $peerHandle")
            client.insertSingleMockedReading("Client")
            val state = client.tagClient.serializedState

            socket.getOutputStream().use { outputStream ->
                val size = state.size
                outputStream.write(ByteBuffer.allocate(4).putInt(size).array())
                outputStream.write(state)
                outputStream.flush()
                socket.shutdownOutput()
            }
            Log.d("DONEEE", "SUBSCRIBE: All information sent we are done")
        } catch (e: IOException) {
            Log.e("Subscriber", "SUBSCRIBE: IOException in handleDataExchange: ${e.message}")
        } catch (e: SecurityException) {
            Log.e("Subscriber", "SUBSCRIBE: SecurityException in handleDataExchange: ${e.message}")
        } catch (e: Exception) {
            Log.e("Subscriber", "SUBSCRIBE: Error in handleDataExchange: ${e.message}")
        } finally {
            try {
                clientSocket?.close()

            } catch (e: IOException) {
                Log.e("Subscriber", "SUBSCRIBE: Error closing socket: ${e.message}")
            }
            // After data exchange, process the next peer
            connectivityManager!!.unregisterNetworkCallback(networkCallbackSub)
            currentPeerHandle = null

            Log.d("Subscriber", "NEWPHONE: we are starting for a new phone because the information is already sent to the prev one in handleDataExchange ${peerHandleQueue.size}")
            processNextPeerHandle()
        }
    }

    private fun sendMessageToPublisher(peerHandle: PeerHandle) {
        CoroutineScope(Dispatchers.IO).launch {
            Log.d("Subscriber", "SEND_MSG: Sending message to publisher with peer handle: $peerHandle")
            try{
                currentSubSession?.sendMessage(
                    peerHandle,
                    0, // Message type (0 for unsolicited)
                    serviceUUID,
                )
            }catch (e : Exception){
                Log.e("1Wifi","Could not send message ${e.message} Stacktrace: ${Log.getStackTraceString(e)}")
            }

        }
    }

    private fun startResponseTimer(peerHandle: PeerHandle) {
        responseTimer?.cancel()
        Log.d("Subscriber", "TIMER: Starting response timer for peer handle: $peerHandle")
        responseTimer = Timer().apply {
            schedule(object : TimerTask() {
                override fun run() {
                    Log.d("Subscriber", "TIMER: Response timeout for peer: $peerHandle")


                    synchronized(peerHandleQueue) {
                        currentPeerHandle = null
                        peerHandleQueue.addLast(peerHandle)
                        Log.d(
                            "Subscriber",
                            "NEWPHONE: we are starting for a new phone because Response timeout in $peerHandle startResponseTimer ${peerHandleQueue.size}"
                        )
                        processNextPeerHandle()

                    }
                }

            }, RESPONSE_TIMEOUT)
        }
    }

    private fun startResponseTimer2(peerHandle: PeerHandle,connectivityManager: ConnectivityManager) {
        responseTimer2?.cancel()
        //Log.d("1Wifi", "TIMER: Starting response timer for peer handle: $peerHandle")
        responseTimer2 = Timer().apply {
            schedule(object : TimerTask() {
                override fun run() {
                    Log.d("Subscriber", "RETRYING CONNECTION WITH $peerHandle in timer 2")
                    //createNetwork(peerHandle,context)
                    connectivityManager!!.unregisterNetworkCallback(networkCallbackSub)
                    peerHandleQueue.addFirst(peerHandle)
                    processNextPeerHandle()
                }

            }, RESPONSE_TIMEOUT)
        }
    }

    private fun processNextPeerHandle() {
        synchronized(peerHandleQueue) {

            //Log.d("1Wifi", "NEWPHONE: we are starting for a new phone ${peerHandleQueue.size}")
            currentPeerHandle = peerHandleQueue.removeFirstOrNull()  // Use poll to remove the head
            currentPeerHandle?.let {
                sendMessageToPublisher(it)
                startResponseTimer(it)
            }

        }
    }
}