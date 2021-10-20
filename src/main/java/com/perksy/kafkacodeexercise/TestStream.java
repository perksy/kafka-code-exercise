package com.perksy.kafkacodeexercise;

import com.fasterxml.jackson.core.type.TypeReference;
import com.perksy.kafkacodeexercise.model.Order;
import com.perksy.kafkacodeexercise.model.OrderItem;
import com.perksy.kafkacodeexercise.model.OrderResult;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.processor.AbstractProcessor;
import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorSupplier;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.serializer.JsonSerde;

import java.math.BigDecimal;
import java.util.*;

@Configuration
public class TestStream {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestStream.class);

//    @Bean
    public KStream<?,?> buildProcessorStream(StreamsBuilder builder) {
        var orderTable = builder.<String, Order>table(KafkaStreamsConfig.ORDER_TOPIC,
        Materialized.with(new Serdes.StringSerde(), new JsonSerde<>(Order.class)));

        var itemsStream = builder.<String, OrderItem>stream(KafkaStreamsConfig.ORDER_ITEM_TOPIC);
        itemsStream.selectKey((key, value) -> value.getOrderId()).join(orderTable, (item, order) -> Pair.of(order, item))
                .process(new ProcessorSupplier<>() {
                    @Override
                    public Processor<String, Pair<Order, OrderItem>> get() {
                        return new AbstractProcessor<>() {
                            @Override
                            public void process(String key, Pair<Order, OrderItem> value) {
                                KeyValueStore<String, OrderResult> store = context.getStateStore("ResultsStore");
                                var order = value.getLeft();
                                var item = value.getRight();
                                var orderId = "";
                                if (order != null) orderId = order.getOrderId();
                                if (item != null) orderId = item.getOrderId();
                                var existingResult = store.get(orderId);
                                if (existingResult == null) existingResult = new OrderResult();
                                if (order != null) {
                                    existingResult.setOrder(order);
                                }
                                if (item != null) {
                                    if (! existingResult.getItems().contains(item)) {
                                        existingResult.getItems().add(item);
                                    }
                                }
                                existingResult.setTotalAmount(
                                        existingResult.getItems().stream().map(orderItem -> orderItem.getItemPrice().multiply(new BigDecimal(orderItem.getItemQuantity())))
                                                .reduce(existingResult.getTotalAmount(), BigDecimal::add)
                                );
                                store.put(orderId, existingResult);
                                LOGGER.info("Stored order with id " + orderId);
                            }
                        };
                    }

                    @Override
                    public Set<StoreBuilder<?>> stores() {
                        return Collections.singleton(
                                Stores.keyValueStoreBuilder(
                                        Stores.persistentKeyValueStore("ResultsStore"),
                                        new Serdes.StringSerde(),
                                        new JsonSerde<>(OrderResult.class)
                                )
                        );
                    }
                });
        return itemsStream;
    }

    @Bean
    public KStream<?, ?> buildStream(StreamsBuilder builder) {
        var ordersStream = builder.<String, Order>stream(KafkaStreamsConfig.ORDER_TOPIC);
        ordersStream.print(Printed.toSysOut());
        var itemsStream = builder.<String, OrderItem>stream(KafkaStreamsConfig.ORDER_ITEM_TOPIC);
        itemsStream.print(Printed.toSysOut());
        var ordersTable = ordersStream.toTable(Named.as("OrdersTable"),
                Materialized.with(new Serdes.StringSerde(), new JsonSerde<>(Order.class)));
        itemsStream.groupBy((key, value) -> value.getOrderId())
                .aggregate((Initializer<HashMap<String, OrderItem>>) HashMap::new, (key, value, aggregate) -> {
                    aggregate.put(value.getOrderItemId(), value);
                    return aggregate;
                }, Materialized.with(new Serdes.StringSerde(), new JsonSerde<>(new TypeReference<Map<String, OrderItem>>() {})))
                .join(ordersTable, (itemsMap, order) -> {
                    var result = new OrderResult();
                    result.setOrder(order);
                    result.setItems(new ArrayList<>(itemsMap.values()));
                    result.setTotalAmount(
                            result.getItems().stream().map(orderItem -> orderItem.getItemPrice().multiply(new BigDecimal(orderItem.getItemQuantity())))
                                    .reduce(result.getTotalAmount(), BigDecimal::add)
                    );
                    return result;
                }, Materialized.<String, OrderResult, KeyValueStore<Bytes, byte[]>>as("ResultsStore")
                        .withKeySerde(new Serdes.StringSerde())
                        .withValueSerde(new JsonSerde<>(OrderResult.class)))
                .toStream().print(Printed.toSysOut());
        return ordersStream;
    }

}
