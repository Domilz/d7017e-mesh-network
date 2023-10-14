package com.epiroc.wifiaware.Screens

import android.Manifest
import android.content.Intent
import android.net.wifi.aware.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.epiroc.wifiaware.Services.WifiAwareService
import com.epiroc.wifiaware.ViewModels.HomeScreenViewModel

val permissionsToRequest = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.NEARBY_WIFI_DEVICES
)
val LocalWifiAwareService = compositionLocalOf<WifiAwareService?> { null }

@Composable
fun ServiceAwareContent(service: WifiAwareService) {
    CompositionLocalProvider(LocalWifiAwareService provides service) {
        HomeScreen()
    }
}
@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val service = LocalWifiAwareService.current
    val intent = Intent(context, WifiAwareService::class.java)
    ContextCompat.startForegroundService(context, intent)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (service != null) {
            Text(text = service.hasWifiAwareText.value)
            Text(text = service.publishMessageLiveData.value)
            Text(text = service.subscribeMessageLiveData.value)
        }
    }
}





