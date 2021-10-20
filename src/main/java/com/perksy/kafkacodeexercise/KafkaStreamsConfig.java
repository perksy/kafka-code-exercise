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

    @EventListener
    @Order(0)
    public void onApplicationEvent(ContextRefreshedEvent event) throws ExecutionException, InterruptedException {
        AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties());
        Map<TopicPartition, OffsetSpec> offsetRequests = new HashMap<>();
        Map<TopicPartition, RecordsToDelete> deletions = new HashMap<>();
        for (int i = 0; i < NUM_PARTITIONS; i++) {
            offsetRequests.put(new TopicPartition(ORDER_TOPIC, i), OffsetSpec.latest());
            offsetRequests.put(new TopicPartition(ORDER_ITEM_TOPIC, i), OffsetSpec.latest());
        }
        ListOffsetsResult listOffsetsResult = adminClient.listOffsets(offsetRequests);
        for (int i = 0; i < NUM_PARTITIONS; i++) {
            TopicPartition orderTopicPartition = new TopicPartition(ORDER_TOPIC, i);
            TopicPartition itemTopicPartition = new TopicPartition(ORDER_ITEM_TOPIC, i);

            deletions.put(orderTopicPartition, RecordsToDelete.beforeOffset(listOffsetsResult.partitionResult(orderTopicPartition).get().offset()));
            deletions.put(itemTopicPartition, RecordsToDelete.beforeOffset(listOffsetsResult.partitionResult(itemTopicPartition).get().offset()));
        }
        DeleteRecordsResult result = adminClient.deleteRecords(deletions);
        result.all().get();
        logger.warn("Deleted all records");
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