package com.perksy.kafkacodeexercise

import com.perksy.kafkacodeexercise.model.Order
import com.perksy.kafkacodeexercise.model.OrderItem
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.common.serialization.Serdes
import org.junit.jupiter.api.assertThrows
import org.springframework.kafka.support.serializer.JsonSerde
import kotlin.test.Test
import kotlin.test.assertIs

class SerdesTests {

    @Test
    fun `test reified serdeFor()`() {
        assertIs<Serdes.StringSerde>(serdeFor<String>())
    }

    @Test
    fun `test reified serdeFor() with generic`() {
        // Incomplete. assertIs type erasure
        assertIs<JsonSerde<Map<String, String>>>(serdeFor<Map<String, String>>())
    }

    @Test
    fun `test serdeForSimpleClass()`() {
        assertIs<Serdes.StringSerde>(serdeForSimpleClass(String::class.java))
    }

    @Test
    fun `test serdeForSimpleClass fails with generic type`() {
        assertThrows<IllegalArgumentException> {
            serdeForSimpleClass(Map::class.java)
        }
    }

    @Test
    fun `test serdeForTypeParam()`() {
        assertIs<JsonSerde<Map<String, String>>>(serdeForJsonType(typeReference<Map<String, String>>()))
    }

}