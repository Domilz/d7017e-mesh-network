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
    //val viewModel = viewModel<HomeScreenViewModel>(factory = HomeScreenViewModelFactory(context, packageManager))

    var acquisitionMessage = viewModel.availability()

    var hasWifiAware by remember { mutableStateOf("Checking Wifi Aware availability...") }
    var publishMessage by remember { mutableStateOf("") }
    var subscribeMessage by remember { mutableStateOf("") }


    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = {
                if (viewModel.checkWifiAwareAvailability()) {
                    publishMessage = "Publish Using Wifi Aware started...:" + wifiAwareSession.toString()
                    viewModel.publishUsingWifiAware()
                } else {
                    hasWifiAware = "Wifi Aware is not available."
                }
            },
            modifier = Modifier.padding(8.dp)
        ) {
            Text("Publish Using Wifi Aware")
        }

        Button(
            onClick = {
                if (viewModel.checkWifiAwareAvailability()) {
                    subscribeMessage = "Subscribe to Wifi Aware Sessions started...:" + currentSession.toString()
                    viewModel.subscribeToWifiAwareSessions()
                } else {
                    hasWifiAware = "Wifi Aware is not available."
                }
            },
            modifier = Modifier.padding(8.dp)
        ) {
            Text("Subscribe to Wifi Aware Sessions")
        }

        Button(
            onClick = {
                navController.navigate(route = Screen.Publish.route)
            },
            modifier = Modifier.padding(8.dp)
        ) {
            Text("Go to Publish Activity")
        }

        Button(
            onClick = {
                navController.navigate(route = Screen.Subscribe.route)

            },
            modifier = Modifier.padding(8.dp)
        ) {
            Text("Go to Subscribe Activity")
        }

        Text(
            text = acquisitionMessage,
            modifier = Modifier.padding(16.dp)
        )

        Text(
            text = hasWifiAware,
            modifier = Modifier.padding(16.dp)
        )

        Text(
            text = publishMessage, // Display the publish message here
            modifier = Modifier.padding(16.dp)
        )

        Text(
            text = subscribeMessage, // Display the subscribe message here
            modifier = Modifier.padding(16.dp)
        )


    }
}


