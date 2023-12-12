package com.epiroc.wifiaware.workers

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.epiroc.wifiaware.lib.Client
import com.epiroc.wifiaware.lib.Config
import com.epiroc.wifiaware.transport.utility.WifiAwareUtility
import java.io.File

class NetworkWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {
    private val utility: WifiAwareUtility = WifiAwareUtility
    private val context = appContext

    override fun doWork(): Result {
        return try {
            utility.sendPostRequest()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}

