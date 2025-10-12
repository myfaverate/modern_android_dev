package io.github.opensllearn.utils

import org.junit.Assert.assertEquals
import org.junit.Test


internal class UtilsTest {
    @Test
    internal fun `test helloTest`(){
        assertEquals("Hello Test", Utils.helloTest())
    }
}