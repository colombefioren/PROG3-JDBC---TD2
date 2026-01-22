package com.jdbctd2.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Dish {
  private Integer id;
  private String name;
  private DishTypeEnum dishType;
  private List<DishIngredient> dishIngredients;
  private Double sellingPrice;

  public Dish() {}

  public Dish(String name, DishTypeEnum dishType, Double sellingPrice) {
    this.name = name;
    this.dishType = dishType;
    this.sellingPrice = sellingPrice;
  }

  public Dish(int id, String name, DishTypeEnum dishType, List<DishIngredient> dishIngredients) {
    this.id = id;
    this.name = name;
    this.dishType = dishType;
    this.dishIngredients = dishIngredients;
  }

  public Dish(String name, DishTypeEnum dishType, List<DishIngredient> dishIngredients) {
    this.name = name;
    this.dishType = dishType;
    this.dishIngredients = dishIngredients;
  }

  public Dish(
      int id,
      String name,
      DishTypeEnum dishType,
      List<DishIngredient> dishIngredients,
      Double sellingPrice) {
    this.id = id;
    this.name = name;
    this.dishType = dishType;
    this.dishIngredients = dishIngredients;
    this.sellingPrice = sellingPrice;
  }

  public Integer getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("Dish name cannot be null or blank");
    }
    this.name = name;
  }

  public DishTypeEnum getDishType() {
    return dishType;
  }

  public void setDishType(DishTypeEnum dishType) {
    this.dishType = dishType;
  }

  public List<DishIngredient> getDishIngredients() {
    return dishIngredients;
  }

  public void setDishIngredients(List<DishIngredient> newDishIngredients) {
    if (this.dishIngredients == null) {
      this.dishIngredients = new ArrayList<>();
    } else {
      this.dishIngredients.clear();
    }
    if (newDishIngredients != null) {
      for (DishIngredient di : newDishIngredients) {
        if (di != null) {
          di.setDish(this);
          this.dishIngredients.add(di);
        }
      }
    }
  }

  public List<Ingredient> getIngredients() {
    List<Ingredient> ingredients = new ArrayList<>();
    for (DishIngredient di : dishIngredients) {
      if (di.getIngredient() != null) {
        ingredients.add(di.getIngredient());
      }
    }
    return ingredients;
  }

  public Double getDishCost() {
    if (dishIngredients == null || dishIngredients.isEmpty()) {
      return 0.0;
    }

    double totalCost = 0.0;
    for (DishIngredient di : dishIngredients) {
      totalCost += di.getCost();
    }
    return totalCost;
  }

  public Double getSellingPrice() {
    return this.sellingPrice;
  }

  public void setSellingPrice(Double price) {
    if (price != null) {
      if (price < 0) {
        throw new IllegalArgumentException("Dish price cannot be negative");
      }
    }
    this.sellingPrice = price;
  }

  public Double getGrossMargin() {
    if (this.sellingPrice == null) {
      throw new IllegalStateException("Cannot calculate gross margin: sale price is not set");
    }
    Double cost = getDishCost();
    return this.sellingPrice - cost;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    Dish dish = (Dish) o;
    return Objects.equals(id, dish.id)
        && Objects.equals(name, dish.name)
        && dishType == dish.dishType
        && Objects.equals(dishIngredients, dish.dishIngredients)
        && Objects.equals(sellingPrice, dish.sellingPrice);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, dishType, dishIngredients, sellingPrice);
  }

  @Override
  public String toString() {
    return "Dish{"
        + "id="
        + id
        + ", name='"
        + name
        + '\''
        + ", dishType="
        + dishType
        + ", ingredients="
        + this.getIngredients()
        + ", sellingPrice="
        + sellingPrice
        + '}';
  }
}
