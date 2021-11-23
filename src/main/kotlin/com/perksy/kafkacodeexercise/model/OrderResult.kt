package com.perksy.kafkacodeexercise.model

import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*

data class OrderResult (
    var order: Order? = null,
    var items: MutableList<OrderItem> = ArrayList(),
    var totalAmount : BigDecimal = BigDecimal(0).setScale(2, RoundingMode.UNNECESSARY)
)