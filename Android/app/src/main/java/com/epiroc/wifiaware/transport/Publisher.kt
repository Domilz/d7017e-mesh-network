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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.ActivityCompat
import androidx.core.app.ComponentActivity
import com.epiroc.wifiaware.Screens.permissionsToRequest
import com.epiroc.wifiaware.transport.network.PublisherNetwork
import com.epiroc.wifiaware.transport.utility.WifiAwareUtility
import java.io.IOException
import java.net.ServerSocket
import java.net.Socket
import java.util.Timer
import java.util.TimerTask

class Publisher(
    ctx: Context,
    nanSession: WifiAwareSession,
    network: PublisherNetwork,
    srvcName: String,
    uuid: String,
    val config: PublishConfig? = null
) {
    private var serviceUUID = uuid
    private var context = ctx
    private var network = network
    private lateinit var currentPubSession: DiscoverySession

    private val serviceName = srvcName
    private val wifiAwareSession = nanSession

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun publishUsingWifiAware() {
        Log.d("1Wifi", "PUBLISH: Attempting to start publishUsingWifiAware.")
        if (wifiAwareSession != null) {
            Log.d("1Wifi", "PUBLISH: ServiceName is set to $serviceName.")

            val config = this.config ?: PublishConfig.Builder()
                .setServiceName(serviceName)
                .build()
            val handler = Handler(Looper.getMainLooper())
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.NEARBY_WIFI_DEVICES
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.d("1Wifi","PUBLISH: NO PREM FOR PUB")
                // Permissions are not granted, request them first.
                ActivityCompat.requestPermissions(
                    this as ComponentActivity, // Cast to ComponentActivity if needed
                    permissionsToRequest,
                    123 // Use a unique request code, e.g., 123
                )
            } else {
                Log.d("1Wifi","PUBLISH: WE HAVE PREM TO PUBLISH")
                // Permissions are granted, proceed with publishing.
                wifiAwareSession!!.publish(config, object : DiscoverySessionCallback() {
                    override fun onPublishStarted(session: PublishDiscoverySession) {
                        Log.d("1Wifi", "PUBLISH: Publish started")
                        currentPubSession = session
                    }
                    override fun onMessageReceived(peerHandle: PeerHandle, message: ByteArray) {
                        Log.d("1Wifi", "PUBLISH: Message received from peer in publisher $peerHandle")
                        //connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                        network.createNetwork(currentPubSession,peerHandle,wifiAwareSession,context)
                        //publishMessageLiveData.value = "PUBLISH: MessageReceived from $peerHandle message: ${message.decodeToString()}"
                        // Respond to the sender (Device A) if needed.
                        //val byteArrayToSend = "tag_id:\"PUBLISH\" readings:{tag_id:\"20\"  device_id:\"21\"  rssi:69  ts:{seconds:1696500095  nanos:85552100}}"
                        Log.d("1Wifi", "PUBLISH: sending message now via publisher to $peerHandle")

                        Timer().schedule(object : TimerTask() {
                            override fun run() {
                                currentPubSession?.sendMessage(
                                    peerHandle,
                                    0, // Message type (0 for unsolicited)
                                    serviceUUID.toByteArray(Charsets.UTF_8)
                                )
                            }
                        }, 1000) // Delay in milliseconds*/
                    }
                }, handler)
            }
        } else {
            Log.d("1Wifi", "PUBLISH: Wifi Aware session is not available.")
        }
    }

    fun closeServerSocket() {
       network.closeServerSocket()
    }
}