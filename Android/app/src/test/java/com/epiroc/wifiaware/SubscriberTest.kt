package com.epiroc.wifiaware
import android.content.Context
import android.net.wifi.aware.DiscoverySessionCallback
import android.net.wifi.aware.PeerHandle
import android.net.wifi.aware.PublishConfig
import android.net.wifi.aware.SubscribeConfig
import android.net.wifi.aware.WifiAwareSession
import android.os.Handler
import android.os.SystemClock
import com.epiroc.wifiaware.transport.Publisher
import com.epiroc.wifiaware.transport.Subscriber
import com.epiroc.wifiaware.transport.utility.DeviceConnection
import com.epiroc.wifiaware.transport.network.PublisherNetwork
import com.epiroc.wifiaware.transport.network.SubscriberNetwork
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import java.lang.Thread.sleep
import java.time.Clock

class SubscriberTest {


    @Test
    fun testSubscribeUsingWifiAware_Success() {
        // Create mock objects for the dependencies
        val mockWifiAwareSession = Mockito.mock(WifiAwareSession::class.java)
        val mockSubscriberNetwork = Mockito.mock(SubscriberNetwork::class.java)
        val mockContext = Mockito.mock(Context::class.java)

        val mockSubscribeConfig = Mockito.mock(SubscribeConfig::class.java)

        // Create a new Subscriber object using the mock objects
        val subscriber = Subscriber(
            ctx = mockContext,
            nanSession = mockWifiAwareSession,
            network = mockSubscriberNetwork,
            srvcName = "MyTestService",
            uuid = "MyTestUUID",
            subscribeConfig = mockSubscribeConfig
        )

        // Call the subscribeUsingWifiAware() method
        subscriber.subscribeToWifiAwareSessions()

        Mockito.verify(mockWifiAwareSession, Mockito.times(1)).subscribe(Mockito.any(SubscribeConfig::class.java), Mockito.any(DiscoverySessionCallback::class.java), Mockito.any(Handler::class.java))
    }

    @Test
    fun shouldConnectToDevice_Success() {
        // Create mock objects for the dependencies
        val mockWifiAwareSession = Mockito.mock(WifiAwareSession::class.java)
        val mockSubscriberNetwork = Mockito.mock(SubscriberNetwork::class.java)
        val mockContext = Mockito.mock(Context::class.java)
        val mockDiscoverySessionCallback = Mockito.mock(DiscoverySessionCallback::class.java)
        val mockSubscribeConfig = Mockito.mock(SubscribeConfig::class.java)

        // Create a mockable clock and set the current time to a specific value
        val mockClock = Mockito.mock(Clock::class.java)
        val currentTimeMillis = SystemClock.uptimeMillis()
        Mockito.`when`(mockClock.millis()).thenReturn(currentTimeMillis)

        // Create a new Subscriber object using the mock objects
        val subscriber = Subscriber(
            ctx = mockContext,
            nanSession = mockWifiAwareSession,
            network = mockSubscriberNetwork,
            srvcName = "MyTestService",
            uuid = "MyTestUUID",
            subscribeConfig = mockSubscribeConfig
        )
        val shouldConnect2 = subscriber.shouldConnectToDevice(deviceIdentifier = "MyTestDevice")
        assertTrue(shouldConnect2)
        // Simulate the discovery of a device
        val mockPeerHandle = Mockito.mock(PeerHandle::class.java)
        val mockServiceSpecificInfo = "MyTestDevice".toByteArray(Charsets.UTF_8)
        val matchFilter = mutableListOf<ByteArray>()
        //subscriber.discoverySessionCallback.onServiceDiscovered(mockPeerHandle, mockServiceSpecificInfo, matchFilter)
        mockDiscoverySessionCallback.onServiceDiscovered(mockPeerHandle, mockServiceSpecificInfo, matchFilter)
        // Simulate the receipt of a message from the discovered device
        val message = "MyTestDevice".toByteArray(Charsets.UTF_8)
        mockDiscoverySessionCallback.onMessageReceived(mockPeerHandle, message)

        // Simulate the existence of a device connection in the utility
        val deviceConnection = DeviceConnection("MyTestDevice", System.currentTimeMillis())
        subscriber.utility.add(deviceConnection)

        // Verify that the shouldConnectToDevice() method returns false since the device has been connected recently
        val shouldConnect = subscriber.shouldConnectToDevice(deviceIdentifier = "MyTestDevice")
        assertFalse(shouldConnect)

        // Use the mock clock to test the behavior of the code
        val deviceConnectionWait = DeviceConnection("MyTestDevice2", 0)
        subscriber.utility.add(deviceConnectionWait)

        //val shouldConnectWait =
        subscriber.shouldConnectToDevice(deviceIdentifier = "MyTestDevice2")
        //assertTrue(shouldConnectWait)

    }

}