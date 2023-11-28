package com.epiroc.wifiaware.lib

import android.content.Context
import android.util.Log
import com.epiroc.wifiaware.R
import org.json.JSONObject
import java.io.InputStream
import java.util.*

object Config {

    private var _configData: JSONObject? = null

    fun loadConfig(context: Context) {
        try {
            val inputStream: InputStream = context.resources.openRawResource(R.raw.config)
            val jsonString = Scanner(inputStream).useDelimiter("\\A").next()
            _configData = JSONObject(jsonString)
            inputStream.close()
        } catch (e: Exception) {
            e.message?.let { Log.e("1Wifi", it) }
        }
    }

    fun getConfigData(): JSONObject?{
        return _configData
    }
}
