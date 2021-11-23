package com.perksy.kafkacodeexercise.model

import java.math.BigDecimal
import java.util.*

data class OrderItem (
    var orderItemId: String,
    var orderId: String,
    var itemPrice: BigDecimal,
    var itemQuantity : Int = 0
)