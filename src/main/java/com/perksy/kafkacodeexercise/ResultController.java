package com.perksy.kafkacodeexercise;

import com.perksy.kafkacodeexercise.model.OrderResult;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ResultController {
    private ObjectProvider<KafkaStreams> streamsProvider;

    public ResultController(ObjectProvider<KafkaStreams> streamsProvider) {
        this.streamsProvider = streamsProvider;
    }

    @GetMapping("/orderresult/{id}")
    OrderResult one(@PathVariable String id) {
        return streamsProvider.getObject().store(
                StoreQueryParameters.fromNameAndType("ResultsStore", QueryableStoreTypes.<String, OrderResult>keyValueStore())
        ).get(id);
    }
}
