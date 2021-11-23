package com.perksy.kafkacodeexercise

import com.perksy.kafkacodeexercise.model.Order
import com.perksy.kafkacodeexercise.model.OrderItem
import com.perksy.kafkacodeexercise.model.OrderResult
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.KStream
import org.apache.kafka.streams.kstream.Printed
import org.apache.kafka.streams.state.KeyValueStore
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile


data class OrderItemPair(val order: Order, val item: OrderItem)

@Configuration
class TestStream {
    @Profile("item-group")
    @Bean
    fun createStreamTopology(builder: StreamsBuilder): KStream<*, *>? {
        builder.stream<String, OrderItem>(KafkaStreamsConfig.ORDER_ITEM_TOPIC)
            .groupBy { _, (_, orderId): OrderItem -> orderId }
            .aggregate(
                { OrderResult() },
                { _, value, acc: OrderResult ->
                    if (value != null)
                        acc.items.add(value)
                    acc.totalAmount = acc.items.map {
                        it.itemPrice * it.itemQuantity.toBigDecimal()
                    }.sumOf { it }
                    acc
                },
                materializedAs("StoreName")
            )
            .toStream()
            .print(Printed.toSysOut())
        return null
    }

    /**
     * Join/Process solution
     */
    @Profile("join-process")
    @Bean
    fun buildProcessorStream(builder: StreamsBuilder): KStream<*, *> {
        val orderTable = builder.table(
            KafkaStreamsConfig.ORDER_TOPIC,
            materialized<String, Order>()
        )
        val itemsStream = builder.stream<String, OrderItem>(KafkaStreamsConfig.ORDER_ITEM_TOPIC)
        itemsStream.selectKey { _: String, (_, orderId): OrderItem -> orderId }
            .join(orderTable) { item: OrderItem, order: Order -> Pair(order, item) }
            .process(
                SimpleProcessorSupplier.builder<String, Pair<Order, OrderItem>>()
                    .addStore<String, OrderResult>("ResultStore")
                    .process { _, value, context ->
                        val store = context.getStateStore<KeyValueStore<String, OrderResult>>("ResultStore")
                        val (order, item) = value
                        val orderId = order.orderId
                        val existingResult = store[orderId] ?: OrderResult()
                        existingResult.order = order
                        if (!existingResult.items.contains(item)) {
                            existingResult.items.add(item)
                        }
                        existingResult.totalAmount =
                            existingResult.items.map {
                                it.itemPrice * it.itemQuantity.toBigDecimal()
                            }.sumOf { it }
                        store.put(orderId, existingResult)
                        LOGGER.info("Stored order with id $orderId")
                    }.build()
            )
        return itemsStream
    }

    /**
     * Join/aggregate solution
     */
    @Profile("join-aggregate")
    @Bean
    fun buildStream(builder: StreamsBuilder ): KStream<*, *> {
        val ordersTable = builder.table<String, Order>(
            KafkaStreamsConfig.ORDER_TOPIC,
            materializedAs("OrdersTable") { it.withCachingDisabled() }
        )
        val itemsTable = builder.table<String, OrderItem>(
            KafkaStreamsConfig.ORDER_ITEM_TOPIC,
            materializedAs("OrderItemsTable") { it.withCachingDisabled() }
        )
        return itemsTable
            .join(
                ordersTable,
                { item -> item.orderId },
                { item, order -> OrderItemPair(order, item)},
                materialized{it.withCachingDisabled()}
            )
            .apply { toStream().print(Printed.toSysOut()) }
            .groupBy { _, pair -> KeyValue(pair.order.orderId, pair) }
            .aggregate(
                {OrderResult()},
                { _, value, agg ->
                    val (order, item) = value
                    if (agg.order == null) agg.order = order
                    agg.items.add(item)
                    agg.totalAmount = agg.items.map{ it.itemPrice * it.itemQuantity.toBigDecimal()}.sumOf{it}
                    agg
                },
                { _, value, agg ->
                    val (_, item) = value
                    agg.items.remove(item)
                    agg.totalAmount = agg.items.map{ it.itemPrice * it.itemQuantity.toBigDecimal()}.sumOf{it}
                    agg
                },
                materializedAs("ResultStore"){it.withCachingDisabled()}
            )
            .toStream().apply { print(Printed.toSysOut()) }
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(TestStream::class.java)
    }
}