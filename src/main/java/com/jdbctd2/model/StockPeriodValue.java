package com.jdbctd2.model;

import java.time.Instant;
import java.util.Objects;

public class StockPeriodValue {
    private int ingredientId;
    private String ingredientName;
    private Instant period;
    private double stockValue;

    public StockPeriodValue(int ingredientId, String ingredientName, Instant period, double stockValue) {
        this.ingredientId = ingredientId;
        this.ingredientName = ingredientName;
        this.period = period;
        this.stockValue = stockValue;
    }


    public StockPeriodValue() {
    }

    public int getIngredientId() {
        return ingredientId;
    }

    public String getIngredientName() {
        return ingredientName;
    }

    public void setIngredientName(String ingredientName) {
        this.ingredientName = ingredientName;
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
        return ingredientId == that.ingredientId && Double.compare(stockValue, that.stockValue) == 0 && Objects.equals(ingredientName, that.ingredientName) && Objects.equals(period, that.period);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ingredientId, ingredientName, period, stockValue);
    }

    @Override
    public String toString() {
        return "StockPeriodValue{" +
                "ingredientId=" + ingredientId +
                ", ingredientName='" + ingredientName + '\'' +
                ", period=" + period +
                ", stockValue=" + stockValue +
                '}';
    }
}

