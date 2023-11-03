package com.epiroc.wifiaware.transport.network

import android.content.Context
import android.net.ConnectivityManager

object ConnectivityManagerHelper {
    private var connectivityManager: ConnectivityManager? = null

    fun getManager(context: Context): ConnectivityManager {
        if (connectivityManager == null) {
            connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        }
        return connectivityManager!!
    }
}