package com.jdbctd2.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Dish {
  private Integer id;
  private String name;
  private DishTypeEnum dishType;
  private List<DishIngredient> dishIngredients;
  private Double price;

  public Dish() {}

  public Dish(Integer id, String name, DishTypeEnum dishType, List<DishIngredient> ingredients) {
    this.id = id;
    this.name = name;
    this.dishType = dishType;
    this.dishIngredients = ingredients;
  }

  public Dish(Integer id, String name, DishTypeEnum dishType) {
    this.id = id;
    this.name = name;
    this.dishType = dishType;
  }

  public Dish(String name, DishTypeEnum dishType, List<DishIngredient> ingredients, Double price) {
    this.name = name;
    this.dishType = dishType;
    this.dishIngredients = ingredients;
    this.price = price;
  }

  public Dish(String name, DishTypeEnum dishType, List<DishIngredient> ingredients) {
    this.name = name;
    this.dishType = dishType;
    this.dishIngredients = ingredients;
  }

  public Dish(String name, DishTypeEnum dishType) {
    this.name = name;
    this.dishType = dishType;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public Double getPrice() {
    return price;
  }

  public void setPrice(Double price) {
    this.price = price;
  }

  public String getName() {
    return name;
  }

  public List<DishIngredient> getDishIngredients() {
    return dishIngredients;
  }

  public List<Ingredient> getIngredients() {
    if (dishIngredients == null || dishIngredients.isEmpty()) {
      return new ArrayList<>();
    }
    return dishIngredients.stream().map(DishIngredient::getIngredient).toList();
  }

  public void setDishIngredients(List<DishIngredient> newDishIngredients) {
    if (this.dishIngredients != null && !this.dishIngredients.isEmpty()) {
      this.dishIngredients.clear();
    }

    this.dishIngredients = newDishIngredients == null ? new ArrayList<>() : newDishIngredients;

    for (DishIngredient dishIngredient : this.dishIngredients) {
      if (dishIngredient != null) {
        dishIngredient.setDish(this);
      }
    }
  }

  public DishTypeEnum getDishType() {
    return dishType;
  }

  public void setDishType(DishTypeEnum dishType) {
    this.dishType = dishType;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Double getDishCost() {
    if (dishIngredients == null || dishIngredients.isEmpty()) {
      return 0.0;
    }

    double cost = 0.0;
    for (DishIngredient dishIngredient : dishIngredients) {
      if (dishIngredient != null
          && dishIngredient.getIngredient() != null
          && dishIngredient.getIngredient().getPrice() != null
          && dishIngredient.getQuantityRequired() != null) {
        cost += dishIngredient.getIngredient().getPrice() * dishIngredient.getQuantityRequired();
      }
    }
    return cost;
  }

  public Double getGrossMargin() {
    if (this.price == null) {
      throw new RuntimeException("Price is null so we cannot calculate gross margin");
    }
    return this.price - this.getDishCost();
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    Dish dish = (Dish) o;
    return Objects.equals(id, dish.id)
        && Objects.equals(name, dish.name)
        && dishType == dish.dishType
        && Objects.equals(dishIngredients, dish.dishIngredients)
        && Objects.equals(price, dish.price);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, dishType, dishIngredients, price);
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
        + getIngredients()
        + ", price="
        + price
        + '}';
  }
}
