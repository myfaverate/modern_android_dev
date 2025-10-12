package io.github.opensllearn

import android.content.Context
import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

private const val TAG: String = "ExampleInstrumentedTest"

/**
 * https://developer.android.com/studio/test/test-in-android-studio?hl=zh-cn
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext: Context = InstrumentationRegistry.getInstrumentation().targetContext
        Log.i(TAG, "useAppContext -> appContext: ${appContext.javaClass}")
        assertEquals("io.github.opensllearn", appContext.packageName)
    }
}