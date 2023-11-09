//import com.epiroc.wifiaware.workers.NetworkWorker
//import org.junit.Assert.assertEquals
//import org.junit.Test
//import java.io.File
//import java.util.*
//import android.content.Context
//import androidx.work.ListenableWorker
//import androidx.work.WorkerParameters
//import org.mockito.Mockito
//import java.nio.file.Files
//
//class NetworkWorkerTest {
//
//    @Test
//    fun testDoWork_Success() {
//        // Create a mock file
//        val file = File.createTempFile("MyState.txt", "")
//        file.writeText("This is my state.")
//        file.flush()
//
//        // Create a mock context
//        val context = Mockito.mock(Context::class.java)
//        Mockito.`when`(context.filesDir).thenReturn(file.parentFile)
//
//        // Create a worker
//        val worker = NetworkWorker(context, Mockito.mock(WorkerParameters::class.java))
//
//        // Call doWork()
//        val result: ListenableWorker.Result = worker.doWork()
//        //val result = worker.doWork()
//
//        // Assert that the result is success
//        assertEquals(Result.success(), result)
//    }
//
//    @Test
//    fun testDoWork_Retry() {
//        // Create a mock context
//        val context = Mockito.mock(Context::class.java)
//        Mockito.`when`(context.filesDir).thenReturn(Files.createTempDirectory("my-temp-dir"))
//
//        // Create a worker
//        val worker = NetworkWorker(context, Mockito.mock(WorkerParameters::class.java))
//
//        // Call doWork()
//        val result = worker.doWork()
//
//        // Assert that the result is retry
//        assertEquals(Result.retry(), result)
//    }
//
//    @Test
//    fun testDoWork_Exception() {
//        // Create a mock context
//        val context = Mockito.mock(Context::class.java)
//        Mockito.`when`(context.filesDir).thenReturn(File.createTempDir())
//
//        // Create a worker
//        val worker = NetworkWorker(context, Mockito.mock(WorkerParameters::class.java))
//
//        // Mock the utility to throw an exception
//        Mockito.`when`(worker.utility.sendPostRequest(Mockito.any())).thenThrow(RuntimeException())
//
//        // Call doWork()
//        val result = worker.doWork()
//
//        // Assert that the result is retry
//        assertEquals(Result.retry(), result)
//    }
//}