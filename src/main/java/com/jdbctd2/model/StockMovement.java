package com.jdbctd2.model;

import java.time.Instant;
import java.util.Objects;

public class StockMovement {
  private Integer id;
  private int quantity;
  private UnitEnum unit;
  private Instant datetime;
  private MovementType type;

  public StockMovement(
      Integer id, int quantity, UnitEnum unit, Instant datetime, MovementType type) {
    this.id = id;
    this.quantity = quantity;
    this.unit = unit;
    this.datetime = datetime;
    this.type = type;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public int getQuantity() {
    return quantity;
  }

  public void setQuantity(int quantity) {
    this.quantity = quantity;
  }

  public UnitEnum getUnit() {
    return unit;
  }

  public void setUnit(UnitEnum unit) {
    this.unit = unit;
  }

  public Instant getDatetime() {
    return datetime;
  }

  public void setDatetime(Instant datetime) {
    this.datetime = datetime;
  }

  public MovementType getType() {
    return type;
  }

  public void setType(MovementType type) {
    this.type = type;
  }

  @Override
  public boolean equals(Object o) {

    if (o == null || getClass() != o.getClass()) return false;
    StockMovement that = (StockMovement) o;
    return quantity == that.quantity
        && Objects.equals(id, that.id)
        && unit == that.unit
        && Objects.equals(datetime, that.datetime)
        && type == that.type;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, quantity, unit, datetime, type);
  }

  @Override
  public String toString() {
    return "StockMovement{"
        + "id="
        + id
        + ", quantity="
        + quantity
        + ", unit="
        + unit
        + ", datetime="
        + datetime
        + ", type="
        + type
        + '}';
  }
}
