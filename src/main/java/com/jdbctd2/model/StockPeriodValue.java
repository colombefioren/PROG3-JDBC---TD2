package com.jdbctd2.model;

import java.time.Instant;
import java.util.Objects;

public class StockPeriodValue {
  private int ingredientId;
  private Instant period;
  private double stockValue;

  public StockPeriodValue(int ingredientId, Instant period, double stockValue) {
    this.ingredientId = ingredientId;
    this.period = period;
    this.stockValue = stockValue;
  }

  public int getIngredientId() {
    return ingredientId;
  }

  public Instant getPeriod() {
    return period;
  }

  public double getStockValue() {
    return stockValue;
  }

  public void setIngredientId(int ingredientId) {
    this.ingredientId = ingredientId;
  }

  public void setPeriod(Instant period) {
    this.period = period;
  }

  public void setStockValue(double stockValue) {
    this.stockValue = stockValue;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    StockPeriodValue that = (StockPeriodValue) o;
    return ingredientId == that.ingredientId
        && Double.compare(stockValue, that.stockValue) == 0
        && Objects.equals(period, that.period);
  }

  @Override
  public int hashCode() {
    return Objects.hash(ingredientId, period, stockValue);
  }

  @Override
  public String toString() {
    return "StockPeriodValue{"
        + "ingredientId="
        + ingredientId
        + ", period="
        + period
        + ", stockValue="
        + stockValue
        + '}';
  }
}
