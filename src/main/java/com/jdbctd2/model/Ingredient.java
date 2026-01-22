package com.jdbctd2.model;

import java.util.List;
import java.util.Objects;

public class Ingredient {
  private Integer id;
  private String name;
  private Double price;
  private CategoryEnum category;
  private List<StockMovement> stockMovementList;

  public Ingredient() {}

  public Ingredient(
      Integer id,
      String name,
      Double price,
      CategoryEnum category,
      List<StockMovement> stockMovements) {
    this.id = id;
    this.name = name;
    this.price = price;
    this.category = category;
    this.stockMovementList = stockMovements;
  }

  public Ingredient(int id, String name, Double price, CategoryEnum category) {
    this.id = id;
    this.name = name;
    this.price = price;
    this.category = category;
  }

  public Ingredient(String name, Double price, CategoryEnum category) {
    this.name = name;
    this.price = price;
    this.category = category;
  }

  public Ingredient(
      String name, Double price, CategoryEnum category, List<StockMovement> stockMovements) {
    this.name = name;
    this.price = price;
    this.category = category;
    this.stockMovementList = stockMovements;
  }

  public Integer getId() {
    return id;
  }

  public void setId(int id) {
    if (id <= 0) {
      throw new IllegalArgumentException("Ingredient id must be positive");
    }
    this.id = id;
  }

  public List<StockMovement> getStockMovements() {
    return stockMovementList;
  }

  public void setStockMovements(List<StockMovement> stockMovements) {
    this.stockMovementList = stockMovements;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("Ingredient name cannot be null or blank");
    }
    this.name = name;
  }

  public Double getPrice() {
    return price;
  }

  public void setPrice(Double price) {
    if (price == null || price < 0) {
      throw new IllegalArgumentException("Ingredient price cannot be negative");
    }
    this.price = price;
  }

  public CategoryEnum getCategory() {
    return category;
  }

  public void setCategory(CategoryEnum category) {
    this.category = category;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    Ingredient that = (Ingredient) o;
    return Objects.equals(id, that.id)
        && Objects.equals(name, that.name)
        && Objects.equals(price, that.price)
        && category == that.category;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, price, category);
  }

  @Override
  public String toString() {
    return "Ingredient{"
        + "id="
        + id
        + ", name='"
        + name
        + '\''
        + ", price="
        + price
        + ", category="
        + category
        + '}';
  }
}
