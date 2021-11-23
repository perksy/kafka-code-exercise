package com.perksy.kafkacodeexercise

import com.fasterxml.jackson.core.type.TypeReference
import com.perksy.kafkacodeexercise.model.OrderResult
import com.perksy.kafkacodeexercise.serdeFor
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.processor.AbstractProcessor
import org.apache.kafka.streams.processor.Processor
import org.apache.kafka.streams.processor.ProcessorContext
import org.apache.kafka.streams.processor.ProcessorSupplier
import org.apache.kafka.streams.state.StoreBuilder
import org.apache.kafka.streams.state.Stores

typealias ProcessFunc<K,V> = (K, V, ProcessorContext) -> Unit
typealias InitFunc = (ProcessorContext?) -> Unit


class SimpleProcessorSupplier<K, V> private constructor(
    private val builder: Builder<K, V>)
    : ProcessorSupplier<K, V> {
    override fun get(): Processor<K, V> = SimpleProcessor<K, V>(builder.process!!, builder.init)
    override fun stores(): MutableSet<StoreBuilder<*>> {
        return builder.stores.map{  (name, storeKey, storeValue) ->
        Stores.keyValueStoreBuilder(
                Stores.persistentKeyValueStore(name),
                serdeFor(storeKey),
                serdeFor(storeValue))
        }.toMutableSet()
    }

    class SimpleProcessor<K, V> constructor(
        private val processFunc : ProcessFunc<K, V>,
        private val initFunc : InitFunc?
    )
        : AbstractProcessor<K, V>() {
        override fun init(context: ProcessorContext?) {
            super.init(context)
            initFunc?.invoke(context)
        }
        override fun process(key: K, value: V) = processFunc(key, value, context)
    }

    /**
     * DON'T CALL. Use SimpleProcessorSupplier.builder() instead
     */
    data class Builder<K, V>constructor(
        private val keyClass: TypeReference<K>,
        private val valueClass: TypeReference<V>,
        var init: InitFunc? = null,
        var process : ProcessFunc<K,V>? = null,
        var stores : MutableList<Triple<String, TypeReference<*>, TypeReference<*>>> = mutableListOf()
    ) {
        fun init(initFunc: InitFunc) = apply{this.init = initFunc}
        fun process(processFunc : ProcessFunc<K, V>) = apply {this.process = processFunc}
        inline fun <reified K, reified V>addStore(storeName : String) = apply {
            stores += Triple(storeName, typeReference<K>(), typeReference<V>())
        }
        fun build() : SimpleProcessorSupplier<K, V> {
            checkNotNull(process)
            return SimpleProcessorSupplier(this)
        }
    }

    companion object {
        inline fun <reified K, reified V> builder() : Builder<K, V> = Builder(typeReference<K>(), typeReference<V>())
    }
}