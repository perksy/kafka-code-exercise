package com.perksy.kafkacodeexercise

import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.KStream
import com.perksy.kafkacodeexercise.model.OrderItem
import org.apache.kafka.streams.kstream.Printed
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class Solution {
    /**
     * Build your stream topology here
     * @return The return value is ignored. Anything built using the builder will be processed
     */
    @Bean
    fun createStreamTopology(builder: StreamsBuilder): KStream<*, *>? {
        builder.stream<String, OrderItem>(KafkaStreamsConfig.ORDER_ITEM_TOPIC)
            .print(Printed.toSysOut())
        return null
    }
}