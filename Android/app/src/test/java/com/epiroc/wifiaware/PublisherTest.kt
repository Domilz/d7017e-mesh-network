import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.aware.WifiAwareSession
import android.text.TextUtils
import androidx.core.app.ActivityCompat
import com.epiroc.wifiaware.transport.Publisher
import com.epiroc.wifiaware.transport.network.PublisherNetwork
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations

class PublisherTest {

    @Test
    fun testPublishUsingWifiAware_Success() {
        // Create mock objects for the dependencies
        val mockWifiAwareSession = Mockito.mock(WifiAwareSession::class.java)
        val mockPublisherNetwork = Mockito.mock(PublisherNetwork::class.java)

        // Create a new Publisher object
        val publisher = Publisher(ctx = Mockito.mock(Context::class.java),
            nanSession = mockWifiAwareSession,
            network = mockPublisherNetwork,
            srvcName = "My Service",
            uuid = "My UUID")

        // Call the publishUsingWifiAware() method
        publisher.publishUsingWifiAware()

        // Verify that the publish session was started
        Mockito.verify(mockWifiAwareSession).publish(Mockito.any(), Mockito.any(), Mockito.any())

        // Verify that the message was sent to the peer device
        Mockito.verify(mockPublisherNetwork).createNetwork(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())
    }

    @Test
    fun testPublishUsingWifiAware_NoPermission() {
        // Create mock objects for the dependencies
        val mockWifiAwareSession = Mockito.mock(WifiAwareSession::class.java)
        val mockPublisherNetwork = Mockito.mock(PublisherNetwork::class.java)

        // Create a new Publisher object
        val publisher = Publisher(ctx = Mockito.mock(Context::class.java),
            nanSession = mockWifiAwareSession,
            network = mockPublisherNetwork,
            srvcName = "My Service",
            uuid = "My UUID")

        // Mock the permission check to return false
        Mockito.`when`(ActivityCompat.checkSelfPermission(Mockito.any(), android.Manifest.permission.ACCESS_FINE_LOCATION)).thenReturn(PackageManager.PERMISSION_DENIED)
        Mockito.`when`(ActivityCompat.checkSelfPermission(Mockito.any(), android.Manifest.permission.NEARBY_WIFI_DEVICES)).thenReturn(PackageManager.PERMISSION_DENIED)

        // Call the publishUsingWifiAware() method
        publisher.publishUsingWifiAware()

        // Verify that the publish session was not started
        Mockito.verify(mockWifiAwareSession, Mockito.never()).publish(Mockito.any(), Mockito.any(), Mockito.any())

        // Verify that the message was not sent to the peer device
        Mockito.verify(mockPublisherNetwork, Mockito.never()).createNetwork(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())
    }
}
