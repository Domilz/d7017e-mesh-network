package com.epiroc.wifiaware.Screens

import android.Manifest
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.epiroc.wifiaware.Services.WifiAwareService

val permissionsToRequest = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.NEARBY_WIFI_DEVICES
)
val LocalWifiAwareService = compositionLocalOf<WifiAwareService?> { null }

@Composable
fun ServiceAwareContent(service: WifiAwareService) {
    CompositionLocalProvider(LocalWifiAwareService provides service) {
        val context = LocalContext.current
        val service = LocalWifiAwareService.current
        val intent = Intent(context, WifiAwareService::class.java)
        ContextCompat.startForegroundService(context, intent)
        if (service != null) {
            HomeScreen(service)
        }
    }
}
@Composable
fun HomeScreen(service:WifiAwareService) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (service != null) {
            Text(text = service.uuidMessageLiveData.value)
            Text(text = service.hasWifiAwareText.value)
            Text(text = service.publishMessageLiveData.value)
            Text(text = service.subscribeMessageLiveData.value)
            Text(text = service.networkMessageLiveData.value)
        }
    }
}




