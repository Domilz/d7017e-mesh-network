import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.aware.DiscoverySession
import android.net.wifi.aware.PeerHandle
import android.net.wifi.aware.PublishConfig
import android.net.wifi.aware.WifiAwareSession
import android.text.TextUtils
import androidx.core.app.ActivityCompat
import com.epiroc.wifiaware.transport.Publisher
import com.epiroc.wifiaware.transport.network.PublisherNetwork
import org.junit.Before
import org.junit.Test
import org.junit.runner.Description
import org.mockito.ArgumentMatcher
import org.mockito.ArgumentMatchers.argThat
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.mockStatic
import org.mockito.MockitoAnnotations



class PublisherTest {
    @Before
    fun initMockito() {
        MockitoAnnotations.openMocks(this)
        // Initialize the mock object


        // Mock the PublishConfig.Builder.setServiceName() method on the spy object

        // Do not mock the PublishConfig.Builder.setServiceName() method
        //Mockito.`when`(PublishConfig.Builder::setServiceName).thenReturn("MyTestService")
    }

    @Test
    fun testPublishUsingWifiAware_Success() {
        // Create mock objects for the dependencies
        val mockWifiAwareSession = Mockito.mock(WifiAwareSession::class.java)
        val mockPublisherNetwork = Mockito.mock(PublisherNetwork::class.java)
        val mockDiscoverySession = Mockito.mock(DiscoverySession::class.java)
        val mockPeerHandle = Mockito.mock(PeerHandle::class.java)
        val mockContext = Mockito.mock(Context::class.java)

        val mockPublishConfig = Mockito.mock(PublishConfig::class.java)

        // Create a new Publisher object using the mock objects
        val publisher = Publisher(
            ctx = mockContext,
            nanSession = mockWifiAwareSession,
            network = mockPublisherNetwork,
            srvcName = "MyTestService",
            uuid = "MyTestUUID",
            config = mockPublishConfig
        )

        // Call the publishUsingWifiAware() method
        publisher.publishUsingWifiAware()

        // Verify that the message was sent to the peer device
        Mockito.verify(mockPublisherNetwork).createNetwork(
            mockDiscoverySession,
            mockPeerHandle,
            mockWifiAwareSession,
            mockContext
        )
    }


    @Test
    fun testPublishUsingWifiAware_NoPermission() {
        // Create mock objects for the dependencies
        val mockWifiAwareSession = Mockito.mock(WifiAwareSession::class.java)
        val mockPublisherNetwork = Mockito.mock(PublisherNetwork::class.java)
        val mockContext = Mockito.mock(Context::class.java)

        // Create a new Publisher object
        val publisher = Publisher(ctx = mockContext,
            nanSession = mockWifiAwareSession,
            network = mockPublisherNetwork,
            srvcName = "My Service",
            uuid = "My UUID",
            config = null)

        // Mock the permission check to return false
        Mockito.`when`(ActivityCompat.checkSelfPermission(mockContext, android.Manifest.permission.ACCESS_FINE_LOCATION)).thenReturn(PackageManager.PERMISSION_DENIED)
        Mockito.`when`(ActivityCompat.checkSelfPermission(mockContext, android.Manifest.permission.NEARBY_WIFI_DEVICES)).thenReturn(PackageManager.PERMISSION_DENIED)

        // Call the publishUsingWifiAware() method
        publisher.publishUsingWifiAware()

        // Verify that the publish session was not started
        Mockito.verify(mockWifiAwareSession, Mockito.never()).publish(Mockito.any(), Mockito.any(), Mockito.any())

        // Verify that the message was not sent to the peer device
        Mockito.verify(mockPublisherNetwork, Mockito.never()).createNetwork(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())
    }
}
