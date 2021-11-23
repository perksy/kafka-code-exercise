package com.perksy.kafkacodeexercise

import org.apache.kafka.common.utils.Bytes
import org.apache.kafka.streams.kstream.Grouped
import org.apache.kafka.streams.kstream.Materialized
import org.apache.kafka.streams.state.KeyValueStore
import org.apache.kafka.streams.state.Stores

typealias BytesKeyValueStore = KeyValueStore<Bytes, ByteArray>

inline fun <reified K, reified V>materializedAs(
    storeName : String,
    noinline config : ((Materialized<K, V, BytesKeyValueStore>) -> Unit)? = null
) : Materialized<K, V, BytesKeyValueStore> =
    Materialized.`as`<K, V>(Stores.persistentKeyValueStore(storeName))
        .withKeySerde(serdeFor())
        .withValueSerde(serdeFor())
        .apply{ config?.invoke(this)}


inline fun <reified K, reified V>materialized(
    noinline config : ((Materialized<K, V, BytesKeyValueStore>) -> Unit)? = null
) : Materialized<K, V, BytesKeyValueStore> {
    return Materialized.with<K, V, BytesKeyValueStore> (serdeFor<K>(), serdeFor<V>()).apply{ config?.invoke(this) }
}