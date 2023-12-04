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
            val file = File(context.filesDir, Config.getConfigData()?.getString("local_storage_file_name"))

            if (file.exists() && file.length() > 0) { // Check if file is not empty
                //utility.sendPostRequest(file.readBytes())client
                utility.sendPostRequest()
                // Clear the file after all lines have been processed.
                file.writeText("")
                Result.success()
            }else{
                Result.retry()
            }

        } catch (e: Exception) {
            Result.retry()
        }
    }
}

