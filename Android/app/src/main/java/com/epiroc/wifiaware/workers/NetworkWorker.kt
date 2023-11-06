package com.epiroc.wifiaware.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.epiroc.wifiaware.transport.utility.WifiAwareUtility
import java.io.File


class NetworkWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {
    private val utility: WifiAwareUtility = WifiAwareUtility
    private val context = appContext
    override fun doWork(): Result {
        return try {
            val file = File(context.filesDir, "MyState.txt")
            if (file.exists() && file.length() > 0) { // Check if file is not empty
                file.forEachLine { line ->
                    if (line.isNotBlank()) { // Check if the line is not blank
                        utility.sendPostRequest(line)
                    }
                }
                file.writeText("")
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }


}

