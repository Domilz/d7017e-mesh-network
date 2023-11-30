package com.epiroc.wifiaware.transport

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.aware.*
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import com.epiroc.wifiaware.lib.Client
import com.epiroc.wifiaware.lib.Config
import com.epiroc.wifiaware.transport.network.ConnectivityManagerHelper
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

class Subscriber(
    ctx: Context,
    nanSession: WifiAwareSession,
    private val client: Client,
    srvcName: String
) {
    private var _currentPeerHandle: PeerHandle? = null
    private var _responseTimer: Timer? = null
    private var _responseTimer2: Timer? = null
    private var _wifiAwareSession = nanSession
    private var _currentSubSession: DiscoverySession? = null

    private val _serviceUUID = client.getClientName().toByteArray()
    private val _context = ctx
    private val _peerHandleQueue = ArrayDeque<PeerHandle>()

    private val _serviceName = srvcName
    private val _responseTimeout = 10000L // 10 seconds for example //TODO: put int config.json

    private lateinit var _networkCallbackSub: ConnectivityManager.NetworkCallback
    private lateinit var  _currentNetwork : Network
    private lateinit var _clientSocket: Socket

    fun subscribeToWifiAwareSessions() {
        val handler = Handler(Looper.getMainLooper()) // Use the main looper.
        Log.d("Subscriber","SUBSCRIBE: subscribeToWifiAwareSessions called")

        val subscribeConfig = SubscribeConfig.Builder()
            .setServiceName(_serviceName)
            .build()

        val discoverySessionCallback = object : DiscoverySessionCallback() {
            override fun onSubscribeStarted(session: SubscribeDiscoverySession) {
                Log.d("Subscriber", "SUBSCRIBE: Subscribe started.")
                _currentSubSession = session
            }

            override fun onServiceDiscovered(
                peerHandle: PeerHandle?,
                serviceSpecificInfo: ByteArray?,
                matchFilter: MutableList<ByteArray>?
            ) {
                Log.d("Subscriber", "SUBSCRIBE: Service discovered from peer: $peerHandle")
                super.onServiceDiscovered(peerHandle, serviceSpecificInfo, matchFilter)

                if (peerHandle != null) {
                    _peerHandleQueue.add(peerHandle)
                    if (_peerHandleQueue.size == 1 && _currentPeerHandle == null) {
                        Log.d("Subscriber", "NEWPHONE: we are starting for a new phone because peerHandleQueue.size == 1 && currentPeerHandle == null in onServiceDiscovered ${_peerHandleQueue.size}")
                        _currentPeerHandle = peerHandle
                        processNextPeerHandle()
                    }
                } else {
                    Log.e("Subscriber", "SUBSCRIBE: PeerHandle is null")
                }
            }

            override fun onMessageSendFailed(messageId: Int) {
                super.onMessageSendFailed(messageId)
                _responseTimer?.cancel()
                _responseTimer2?.cancel()
                Log.e("Subscriber", "SUBSCRIBE: BUUUUUUUUUUU onMessageSendFailed (this is bad) $messageId")
            }

            override fun onMessageReceived(peerHandle: PeerHandle, message: ByteArray) {
                Log.d("Subscriber", "SUBSCRIBE MSG_RECEIVED: Message received from peer: $peerHandle")
                _responseTimer?.cancel()

                synchronized(_peerHandleQueue) {
                    if (_currentPeerHandle == peerHandle) {
                        CoroutineScope(Dispatchers.IO).launch {
                            createNetwork(peerHandle, _context)
                        }
                    }
                }
            }
        }

        if (ActivityCompat.checkSelfPermission(
                _context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            _wifiAwareSession.subscribe(subscribeConfig, discoverySessionCallback, handler)
        }
    }

    fun createNetwork(peerHandle : PeerHandle, context : Context) {
        Log.d("Subscriber", "SUBSCRIBE: Attempting to establish connection with peer: $peerHandle")
        val connectivityManager = ConnectivityManagerHelper.getManager(context)

        val networkSpecifier = _currentSubSession?.let {
            WifiAwareNetworkSpecifier.Builder(it, peerHandle)
                .setPskPassphrase(Config.getConfigData()!!.getString("discoveryPassphrase"))
                .build()
        }

        val myNetworkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI_AWARE)
            .setNetworkSpecifier(networkSpecifier)
            .build()
        Log.d("NETWORKWIFI","SUBSCRIBER: All necessary wifiaware network things created now awaiting callback")

        _networkCallbackSub = object : ConnectivityManager.NetworkCallback() {
            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                Log.d("NETWORKWIFI","SUBSCRIBER: onCapabilitiesChanged")
                Log.d("Subscriber", "SUBSCRIBE: Network capabilities changed for peer: $peerHandle")
                super.onCapabilitiesChanged(network, networkCapabilities)

                if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI_AWARE)) {
                    Log.d("NetworkCallback", "Wi-Fi Aware capabilities changed.")
                    if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
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
                                _clientSocket = network.socketFactory.createSocket()
                                _clientSocket.soTimeout = 5000 // Set socket timeout (10 seconds for example)
                                Log.d("Subscriber","SUBSCRIBER: TRYING TO CONNECT! to port $port")
                                _clientSocket.connect(InetSocketAddress(peerIpv6Addr, port))
                                handleDataExchange(peerHandle, _clientSocket, connectivityManager)
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
                _currentNetwork = network
            }

            override fun onLost(network: Network) {
                connectivityManager.unregisterNetworkCallback(_networkCallbackSub)
                Log.d("Subscriber", "SUBSCRIBE: Network lost for peer: $peerHandle, subscriber restarted")
            }
        }
        connectivityManager.requestNetwork(myNetworkRequest, _networkCallbackSub)
        startResponseTimer2(peerHandle,connectivityManager)
    }

    private fun handleDataExchange(peerHandle: PeerHandle, socket: Socket, connectivityManager: ConnectivityManager) {
        try {
            _responseTimer2?.cancel()
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
                _clientSocket.close()

            } catch (e: IOException) {
                Log.e("Subscriber", "SUBSCRIBE: Error closing socket: ${e.message}")
            }
            connectivityManager.unregisterNetworkCallback(_networkCallbackSub)
            _currentPeerHandle = null
            Log.d("Subscriber", "NEWPHONE: we are starting for a new phone because the information is already sent to the prev one in handleDataExchange ${_peerHandleQueue.size}")
            processNextPeerHandle()
        }
    }

    private fun sendMessageToPublisher(peerHandle: PeerHandle) {
        CoroutineScope(Dispatchers.IO).launch {
            Log.d("Subscriber", "SEND_MSG: Sending message to publisher with peer handle: $peerHandle")
            try{
                _currentSubSession?.sendMessage(
                    peerHandle,
                    0, // Message type (0 for unsolicited)
                    _serviceUUID,
                )
            }catch (e : Exception){
                Log.e("1Wifi","Could not send message ${e.message} Stacktrace: ${Log.getStackTraceString(e)}")
            }
        }
    }

    private fun startResponseTimer(peerHandle: PeerHandle) {
        _responseTimer?.cancel()
        Log.d("Subscriber", "TIMER: Starting response timer for peer handle: $peerHandle")
        _responseTimer = Timer().apply {
            schedule(object : TimerTask() {
                override fun run() {
                    Log.d("Subscriber", "TIMER: Response timeout for peer: $peerHandle")

                    synchronized(_peerHandleQueue) {
                        _currentPeerHandle = null
                        _peerHandleQueue.addLast(peerHandle)
                        Log.d(
                            "Subscriber",
                            "NEWPHONE: we are starting for a new phone because Response timeout in $peerHandle startResponseTimer ${_peerHandleQueue.size}"
                        )
                        processNextPeerHandle()
                    }
                }
            }, _responseTimeout)
        }
    }

    private fun startResponseTimer2(peerHandle: PeerHandle,connectivityManager: ConnectivityManager) {
        _responseTimer2?.cancel()
        _responseTimer2 = Timer().apply {
            schedule(object : TimerTask() {
                override fun run() {
                    Log.d("Subscriber", "RETRYING CONNECTION WITH $peerHandle in timer 2")
                    connectivityManager.unregisterNetworkCallback(_networkCallbackSub)
                    _peerHandleQueue.addFirst(peerHandle)
                    processNextPeerHandle()
                }
            }, _responseTimeout)
        }
    }

    private fun processNextPeerHandle() {
        synchronized(_peerHandleQueue) {
            _currentPeerHandle = _peerHandleQueue.removeFirstOrNull()  // Use poll to remove the head
            _currentPeerHandle?.let {
                sendMessageToPublisher(it)
                startResponseTimer(it)
            }
        }
    }
}