package com.epiroc.wifiaware.Screens

import android.Manifest
import android.net.wifi.aware.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.epiroc.wifiaware.Screen
import com.epiroc.wifiaware.ViewModels.HomeScreenViewModel

var wifiAwareSession: WifiAwareSession? = null
val permissionsToRequest = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.NEARBY_WIFI_DEVICES
)

private var currentSession: SubscribeDiscoverySession? = null
private val connectedDevices = mutableListOf<PeerHandle>()
var wifiAwareManager: WifiAwareManager? = null
lateinit var navController: NavHostController
@Composable
fun HomeScreen(navController: NavHostController, viewModel: HomeScreenViewModel){
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = {
                if (viewModel.checkWifiAwareAvailability()) {
                    viewModel.currentPubString.value = "Publish Using Wifi Aware started...:" + viewModel.currentPubSession.toString()
                    viewModel.publishUsingWifiAware()

                } else {
                    viewModel.hasWifiAwareText.value = "Wifi Aware is not available."
                }
            },
            modifier = Modifier.padding(8.dp)
        ) {
            Text("Publish Using Wifi Aware")
        }

        Button(
            onClick = {
                if (viewModel.checkWifiAwareAvailability()) {

                    viewModel.currentSubString.value = "Subscribe to Wifi Aware Sessions started...:" + viewModel.currentSubSession.toString()
                    viewModel.subscribeToWifiAwareSessions()
                } else {
                    viewModel.hasWifiAwareText.value = "Wifi Aware is not available."
                }
            },
            modifier = Modifier.padding(8.dp)
        ) {
            Text("Subscribe Using Wifi Aware")
        }


        Text(
            text = viewModel.availability(),
            modifier = Modifier.padding(16.dp)
        )

        Text(
            text = viewModel.hasWifiAwareText.value,
            modifier = Modifier.padding(16.dp)
        )

        Text(
            text = viewModel.publishMessageLiveData.value,
            modifier = Modifier.padding(16.dp)
        )

        Text(
            text = viewModel.subscribeMessageLiveData.value,
            modifier = Modifier.padding(16.dp)
        )


    }
}


