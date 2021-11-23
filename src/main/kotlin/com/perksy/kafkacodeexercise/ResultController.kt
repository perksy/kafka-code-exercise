package com.perksy.kafkacodeexercise

import org.springframework.web.bind.annotation.RestController
import org.springframework.beans.factory.ObjectProvider
import org.apache.kafka.streams.KafkaStreams
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import com.perksy.kafkacodeexercise.model.OrderResult
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore
import org.apache.kafka.streams.StoreQueryParameters
import org.apache.kafka.streams.state.QueryableStoreTypes

@RestController
class ResultController(private val streamsProvider: ObjectProvider<KafkaStreams>) {
    val store : ReadOnlyKeyValueStore<String, OrderResult> get() =
        streamsProvider.getObject().store(
            StoreQueryParameters.fromNameAndType(
                "ResultStore",
                QueryableStoreTypes.keyValueStore<String, OrderResult>()
            )
        )

    @GetMapping("/orderresult/{id}")
    fun one(@PathVariable id: String): OrderResult {
        return store[id]
    }
}