package com.jdbctd2.model;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

public class Order {
  private Integer id;
  private String reference;
  private Instant creationDatetime;
  private List<DishOrder> dishOrders;
  private TableOrder tableOrder;

  public Order() {}

  public Order(Integer id, String reference, Instant creationDatetime, List<DishOrder> dishOrders) {
    this.id = id;
    this.reference = reference;
    this.creationDatetime = creationDatetime;
    this.dishOrders = dishOrders;
  }

  public Order(String reference, Instant creationDatetime, List<DishOrder> dishOrders) {
    this.reference = reference;
    this.creationDatetime = creationDatetime;
    this.dishOrders = dishOrders;
  }

  public Order(
      Integer id,
      String reference,
      Instant creationDatetime,
      List<DishOrder> dishOrders,
      TableOrder tableOrder) {
    this.id = id;
    this.reference = reference;
    this.creationDatetime = creationDatetime;
    this.dishOrders = dishOrders;
    this.tableOrder = tableOrder;
  }

  public Order(
      String reference,
      Instant creationDatetime,
      List<DishOrder> dishOrders,
      TableOrder tableOrder) {
    this.reference = reference;
    this.creationDatetime = creationDatetime;
    this.dishOrders = dishOrders;
    this.tableOrder = tableOrder;
  }

  public TableOrder getTableOrder() {
    return tableOrder;
  }

  public void setTableOrder(TableOrder tableOrder) {
    this.tableOrder = tableOrder;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getReference() {
    return reference;
  }

  public void setReference(String reference) {
    this.reference = reference;
  }

  public Instant getCreationDatetime() {
    return creationDatetime;
  }

  public void setCreationDatetime(Instant creationDatetime) {
    this.creationDatetime = creationDatetime;
  }

  public List<DishOrder> getDishOrders() {
    return dishOrders;
  }

  public void setDishOrders(List<DishOrder> dishOrders) {
    this.dishOrders = dishOrders;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    Order order = (Order) o;
    return Objects.equals(id, order.id)
        && Objects.equals(reference, order.reference)
        && Objects.equals(creationDatetime, order.creationDatetime)
        && Objects.equals(dishOrders, order.dishOrders)
        && Objects.equals(tableOrder, order.tableOrder);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, reference, creationDatetime, dishOrders, tableOrder);
  }

  @Override
  public String toString() {
    return "Order{"
        + "id="
        + id
        + ", reference='"
        + reference
        + '\''
        + ", creationDatetime="
        + creationDatetime
        + ", dishOrders="
        + dishOrders
        + ", tableOrder="
        + tableOrder
        + '}';
  }
}
