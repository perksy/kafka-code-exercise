package com.perksy.kafkacodeexercise;

import org.apache.kafka.clients.admin.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.streams.KafkaStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.kafka.StreamsBuilderFactoryBeanCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.core.CleanupConfig;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Configuration
@EnableKafkaStreams
public class KafkaStreamsConfig {

    public static final String ORDER_TOPIC = "order";
    public static final String ORDER_ITEM_TOPIC = "order-item";
    public static final int NUM_PARTITIONS = 3;

    private static final Logger logger = LoggerFactory.getLogger(KafkaStreamsConfig.class);

    private final KafkaAdmin kafkaAdmin;

    private KafkaStreams streams;

    public KafkaStreamsConfig(KafkaAdmin kafkaAdmin) {
        this.kafkaAdmin = kafkaAdmin;
    }

    @Bean
    public NewTopic createOrderTopic() {
        return new NewTopic(ORDER_TOPIC, NUM_PARTITIONS, (short) 1);
    }

    @Bean
    public NewTopic createOrderItemTopic() {
        return new NewTopic(ORDER_ITEM_TOPIC, 3, (short) 1);
    }

    @Bean
    public StreamsBuilderFactoryBeanCustomizer customizeStreamsBuilder() {
        return factoryBean -> {
            factoryBean.setStateListener( ((newState, oldState) -> {
                if (streams != null) return;
                // Don't let anything touch KafkaStreams until it's started up
                if (newState.isRunningOrRebalancing()) {
                    streams = factoryBean.getKafkaStreams();
                }
            }));
        };
    }

    /**
     * Note: Some features of Kafka Streams aren't available until it finishes starting up
     */
    @Bean
    @Scope("prototype")
    public KafkaStreams getStreamsIfRunning() {
        return streams;
    }

}