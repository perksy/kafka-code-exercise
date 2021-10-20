package com.perksy.kafkacodeexercise.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class OrderResult {
    private Order order;
    private List<OrderItem> items = new ArrayList<>();
    private BigDecimal totalAmount = new BigDecimal(0).setScale(2, RoundingMode.UNNECESSARY);

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderResult that = (OrderResult) o;
        return Objects.equals(order, that.order) && Objects.equals(items, that.items) && Objects.equals(totalAmount, that.totalAmount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(order, items, totalAmount);
    }

    @Override
    public String toString() {
        return "OrderResult{" +
                "order=" + order +
                ", items=" + items +
                ", totalAmount=" + totalAmount +
                '}';
    }
}
