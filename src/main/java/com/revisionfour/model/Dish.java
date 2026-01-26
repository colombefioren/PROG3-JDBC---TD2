package com.revisionfour.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Dish {
  private Integer id;
  private String name;
  private DishTypeEnum dishType;
  private List<Ingredient> ingredients;
  private Double price;

  public Dish() {}

  public Dish(Integer id, String name, DishTypeEnum dishType, List<Ingredient> ingredients) {
    this.id = id;
    this.name = name;
    this.dishType = dishType;
    this.ingredients = ingredients;
  }

  public Dish(String name, DishTypeEnum dishType, List<Ingredient> ingredients, Double price) {
    this.name = name;
    this.dishType = dishType;
    this.ingredients = ingredients;
    this.price = price;
  }

  public Dish(String name, DishTypeEnum dishType, List<Ingredient> ingredients) {
    this.name = name;
    this.dishType = dishType;
    this.ingredients = ingredients;
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

  public void setName(String name) {
    this.name = name;
  }

  public DishTypeEnum getDishType() {
    return dishType;
  }

  public void setDishType(DishTypeEnum dishType) {
    this.dishType = dishType;
  }

  public List<Ingredient> getIngredients() {
    return ingredients;
  }

  public void setIngredients(List<Ingredient> newIngredients) {
    if (this.ingredients != null) {
      for (Ingredient ingredient : this.ingredients) {
        ingredient.setDish(null);
      }
      this.ingredients.clear();
    }

    this.ingredients = newIngredients == null ? new ArrayList<>() : newIngredients;

    for (Ingredient newIngredient : this.ingredients) {
      if (newIngredient != null) newIngredient.setDish(this);
    }
  }

  public Double getDishCost() {
    Double cost = 0.0;
    for (Ingredient ingredient : ingredients) {
      cost += ingredient.getPrice();
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
        && Objects.equals(ingredients, dish.ingredients)
        && Objects.equals(price, dish.price);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, dishType, ingredients, price);
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
        + ingredients
        + ", price="
        + price
        + '}';
  }
}
