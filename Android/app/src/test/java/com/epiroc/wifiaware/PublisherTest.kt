import android.content.Context
import android.net.wifi.aware.DiscoverySessionCallback
import android.net.wifi.aware.PublishConfig
import android.net.wifi.aware.WifiAwareSession
import android.os.Handler
import com.epiroc.wifiaware.transport.Publisher
import com.epiroc.wifiaware.transport.network.PublisherNetwork
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.MockitoAnnotations



class PublisherTest {
    @Before
    fun initMockito() {
        MockitoAnnotations.openMocks(this)
    }
    @Test
    fun testPublishUsingWifiAware_Success() {
        // Create mock objects for the dependencies
        val mockWifiAwareSession = Mockito.mock(WifiAwareSession::class.java)
        val mockPublisherNetwork = Mockito.mock(PublisherNetwork::class.java)
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

        Mockito.verify(mockWifiAwareSession, Mockito.times(1)).publish(Mockito.any(PublishConfig::class.java), Mockito.any(DiscoverySessionCallback::class.java), Mockito.any(Handler::class.java))
    }

}
