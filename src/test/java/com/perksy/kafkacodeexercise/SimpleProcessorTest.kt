package com.perksy.kafkacodeexercise

import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import kotlin.test.assertEquals

class SimpleProcessorTest {

    @Test
    fun `test create from builder`() {
        var testKey: String? = null
        var testValue: String? = null
        val supplier = SimpleProcessorSupplier
            .builder<String, String>()
            .process{key, value, _ -> testKey = key; testValue = value }
            .build()

        // need to call init again to pass a context
        supplier.get().apply {init(mock())}.process("Key", "Value")
        assertEquals("Key", testKey)
        assertEquals("Value", testValue)
    }

}