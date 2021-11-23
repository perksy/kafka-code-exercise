package com.perksy.kafkacodeexercise.model

import java.math.BigDecimal
import java.util.*

data class Order (
    val orderId: String,
    val status: OrderStatus
)