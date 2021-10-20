package com.perksy.kafkacodeexercise.model;

import java.math.BigDecimal;
import java.util.Objects;

public class OrderItem {
    private String orderItemId;
    private String orderId;
    private BigDecimal itemPrice;
    private int itemQuantity;

    public String getOrderItemId() {
        return orderItemId;
    }

    public void setOrderItemId(String orderItemId) {
        this.orderItemId = orderItemId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public BigDecimal getItemPrice() {
        return itemPrice;
    }

    public void setItemPrice(BigDecimal itemPrice) {
        this.itemPrice = itemPrice;
    }

    public int getItemQuantity() {
        return itemQuantity;
    }

    public void setItemQuantity(int itemQuantity) {
        this.itemQuantity = itemQuantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderItem orderItem = (OrderItem) o;
        return itemQuantity == orderItem.itemQuantity && Objects.equals(orderItemId, orderItem.orderItemId) && Objects.equals(orderId, orderItem.orderId) && Objects.equals(itemPrice, orderItem.itemPrice);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderItemId, orderId, itemPrice, itemQuantity);
    }

    @Override
    public String toString() {
        return "OrderItem{" +
                "orderItemId='" + orderItemId + '\'' +
                ", orderId='" + orderId + '\'' +
                ", itemPrice=" + itemPrice +
                ", itemQuantity=" + itemQuantity +
                '}';
    }
}
