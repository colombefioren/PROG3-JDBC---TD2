package com.jdbctd2.model;

import com.jdbctd2.model.enums.CategoryEnum;
import com.jdbctd2.model.enums.MovementTypeEnum;
import com.jdbctd2.model.enums.UnitType;
import com.jdbctd2.util.UnitService;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Ingredient {
  private Integer id;
  private String name;
  private CategoryEnum category;
  private Double price;
  private List<StockMovement> stockMovementList;
  private transient UnitConverter unitConverter;

  public Ingredient() {}

  public Ingredient(Integer id, String name, CategoryEnum category, Double price) {
    this.id = id;
    this.name = name;
    this.category = category;
    this.price = price;
    this.unitConverter = UnitService.getConverter(name);
  }

  public Ingredient(String name, CategoryEnum category, Double price) {
    this.name = name;
    this.category = category;
    this.price = price;
    this.unitConverter = UnitService.getConverter(name);
  }

  public Ingredient(
      Integer id,
      String name,
      CategoryEnum category,
      Double price,
      List<StockMovement> stockMovementList) {
    this.id = id;
    this.name = name;
    this.category = category;
    this.price = price;
    this.stockMovementList = stockMovementList;
    this.unitConverter = UnitService.getConverter(name);
  }

  public Ingredient(
      String name, CategoryEnum category, Double price, List<StockMovement> stockMovementList) {
    this.name = name;
    this.category = category;
    this.price = price;
    this.stockMovementList = stockMovementList;
    this.unitConverter = UnitService.getConverter(name);
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public Double getPrice() {
    return price;
  }

  public void setPrice(Double price) {
    this.price = price;
  }

  public void setName(String name) {
    this.name = name;
  }

  public CategoryEnum getCategory() {
    return category;
  }

  public void setCategory(CategoryEnum category) {
    this.category = category;
  }

  public List<StockMovement> getStockMovementList() {
    return stockMovementList;
  }

  public void setStockMovementList(List<StockMovement> stockMovementList) {
    this.stockMovementList = stockMovementList;
  }

  public StockValue getStockValueAt(Instant instant) {
    double total = 0.0;
    List<StockMovement> movementsOfInstant = new ArrayList<>();
    for (StockMovement movement : stockMovementList) {
      if (!movement.getCreationDatetime().isAfter(instant)) {
        movementsOfInstant.add(movement);
      }
    }
    for (StockMovement movement : movementsOfInstant) {
      double quantityInKG =
          convertToKG(movement.getValue().getQuantity(), movement.getValue().getUnit());

      if (movement.getType().equals(MovementTypeEnum.IN)) {
        total += quantityInKG;
      } else if (movement.getType().equals(MovementTypeEnum.OUT)) {
        total -= quantityInKG;
      }
    }
    if (movementsOfInstant.isEmpty()) {
      return new StockValue(0.0, UnitType.KG); // default unit
    }
    return new StockValue(total, UnitType.KG);
  }

  public double convertToKG(double quantity, UnitType unit) {
    if (unitConverter == null) {
      unitConverter = UnitService.getConverter(name);
    }

    if (!unitConverter.supportsUnit(unit)) {
      throw new IllegalArgumentException(unit + " is not a valid unit for " + this.name);
    }
    return unitConverter.convertToKG(quantity, unit);
  }

  @Override
  public boolean equals(Object o) {

    if (o == null || getClass() != o.getClass()) return false;
    Ingredient that = (Ingredient) o;
    return Objects.equals(id, that.id)
        && Objects.equals(name, that.name)
        && category == that.category
        && Objects.equals(price, that.price)
        && Objects.equals(stockMovementList, that.stockMovementList);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, category, price, stockMovementList);
  }

  @Override
  public String toString() {
    return "Ingredient{"
        + "id="
        + id
        + ", name='"
        + name
        + '\''
        + ", category="
        + category
        + ", price="
        + price
        + ", stockMovementList="
        + stockMovementList
        + '}';
  }
}
