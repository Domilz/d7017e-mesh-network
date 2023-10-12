import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.epiroc.wifiaware.MainActivity
import com.epiroc.wifiaware.R
import com.epiroc.wifiaware.ViewModels.HomeScreenViewModel

class WifiAwareService : Service() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private lateinit var viewModel: HomeScreenViewModel

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "WifiAware Service Channel"
            val description = "Channel for WifiAware foreground service"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, channelName, importance).apply {
                this.description = description
            }

            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "wifiAwareServiceChannel"
    }



    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Show notification for the foreground service
        val intent = Intent(this, MainActivity::class.java).apply {
            flags
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE)

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("WifiAware Service Running")
            .setContentText("Service is running in the background...")
            //.setSmallIcon(R.drawable.ic_your_icon) // Replace with your app's icon
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()

        startForeground(1, notification)

        // Initialize the ViewModel
        viewModel = HomeScreenViewModel(this, packageManager)

        // Start publish and subscribe tasks
        if (viewModel.checkWifiAwareAvailability()) {
            viewModel.publishUsingWifiAware()
            viewModel.subscribeToWifiAwareSessions()
        } else {
            Log.d("WifiAwareService", "Wifi Aware is not available.")
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        // Handle any cleanup if necessary
    }
}
