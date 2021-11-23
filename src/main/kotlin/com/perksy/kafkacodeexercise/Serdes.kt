package com.perksy.kafkacodeexercise

import com.fasterxml.jackson.core.type.TypeReference
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.common.serialization.Serdes
import org.springframework.kafka.support.serializer.JsonSerde


inline fun <reified T> typeReference() = object : TypeReference<T>(){}


inline fun <reified T>serdeFor() : Serde<T> {
    return serdeFor(typeReference())
}

fun <T>serdeFor(typeReference: TypeReference<T>) : Serde<T>{
    return try {
        @Suppress("UNCHECKED_CAST")
        Serdes.serdeFrom(typeReference.type as Class<T>)
    }
    catch (e : Exception) {
        when(e) {
            is IllegalArgumentException, is ClassCastException -> JsonSerde(typeReference)
            else -> throw e
        }
    }
}

/**
 * This will not work for classes with type parameters
 */
fun <T>serdeForSimpleClass(clazz: Class<T>) : Serde<T> {
    if (clazz.typeParameters.isNotEmpty())
        throw IllegalArgumentException("Cannot get serde for class with type parameters. " +
                "Use reified or TypeReference override instead")
    return try { Serdes.serdeFrom(clazz) }
    catch (e : Exception) {
        when(e) {
            is IllegalArgumentException, is ClassCastException -> JsonSerde(clazz)
            else -> throw e
        }
    }
}

fun <T>serdeForJsonType(typeReference: TypeReference<T>) : Serde<T> {
    return JsonSerde(typeReference)
}

inline fun <reified T>serdeForJsonType() : Serde<T> {
    return JsonSerde(typeReference<T>())
}