package com.perksy.kafkacodeexercise;

import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Solution {

    /**
     * Build your stream topology here
     * @return The return value is ignored. Anything built using the builder will be processed
     */
    @Bean
    public KStream<?,?> createStreamTopology(StreamsBuilder builder) {
        builder.stream(KafkaStreamsConfig.ORDER_ITEM_TOPIC);
        return null;
    }

}
