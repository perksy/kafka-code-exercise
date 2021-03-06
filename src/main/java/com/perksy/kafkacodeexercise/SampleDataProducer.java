package com.perksy.kafkacodeexercise;

import com.github.javafaker.Faker;
import com.perksy.kafkacodeexercise.model.Order;
import com.perksy.kafkacodeexercise.model.OrderItem;
import com.perksy.kafkacodeexercise.model.OrderStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Component
public class SampleDataProducer {

    private static final Logger logger = LoggerFactory.getLogger(SampleDataProducer.class);

    private static final String TARGET_ORDER_ID="71cacbff-0650-40df-8420-237d0fdd6827";

    @Autowired
    public SampleDataProducer(
            KafkaTemplate<String, Object> kafkaTemplate
    ) {
        this.kafkaTemplate = kafkaTemplate;
    }

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) throws ExecutionException, InterruptedException {
        seedSampleData();
    }

    public void seedSampleData() throws ExecutionException, InterruptedException {
        logger.warn("Beginning data seed");
        Faker faker = new Faker();
        var orderCount = 100;
        var orderIds = new String[orderCount];
        Arrays.setAll(orderIds, (i) -> faker.internet().uuid());
        orderIds[0] = TARGET_ORDER_ID;
        var orderIdList = Arrays.asList(orderIds);
        Collections.shuffle(orderIdList);

        final LinkedList<ListenableFuture<?>> futures = new LinkedList<>();
        for (String id: orderIdList
             ) {
            var order = new Order(id, OrderStatus.pending);
            futures.add(kafkaTemplate.send(KafkaStreamsConfig.ORDER_TOPIC, id , order));
            var numItems = faker.number().numberBetween(1, 10);
            for (int i = 0; i < numItems; i++) {
                var amountCents = faker.number().numberBetween(10, 10000);
                var amountDollars = new BigDecimal(amountCents).movePointLeft(2);
                var orderItem = new OrderItem(
                    faker.internet().uuid(), id, amountDollars, faker.number().numberBetween(1, 5)
                );
                futures.add(kafkaTemplate.send(KafkaStreamsConfig.ORDER_ITEM_TOPIC, orderItem.getOrderItemId() , orderItem));
            }

        }
        CompletableFuture.allOf(futures.stream().map(ListenableFuture::completable).toArray(CompletableFuture[]::new)).get();
        logger.warn("Seeded sample Data");
    }



}
