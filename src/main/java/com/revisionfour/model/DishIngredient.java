package com.revisionfour.model;

import java.util.Objects;

public class DishIngredient {
  private Integer id;
  private Dish dish;
  private Ingredient ingredient;
  private Double quantityRequired;
  private UnitType unit;

  public DishIngredient() {}

  public DishIngredient(
      Integer id, Dish dish, Ingredient ingredient, Double quantityRequired, UnitType unit) {
    this.id = id;
    this.dish = dish;
    this.ingredient = ingredient;
    this.quantityRequired = quantityRequired;
    this.unit = unit;
  }

  public DishIngredient(Dish dish, Ingredient ingredient, Double quantityRequired, UnitType unit) {
    this.dish = dish;
    this.ingredient = ingredient;
    this.quantityRequired = quantityRequired;
    this.unit = unit;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public Dish getDish() {
    return dish;
  }

  public void setDish(Dish dish) {
    this.dish = dish;
  }

  public Ingredient getIngredient() {
    return ingredient;
  }

  public void setIngredient(Ingredient ingredient) {
    this.ingredient = ingredient;
  }

  public Double getQuantityRequired() {
    return quantityRequired;
  }

  public void setQuantityRequired(Double quantityRequired) {
    this.quantityRequired = quantityRequired;
  }

  public UnitType getUnit() {
    return unit;
  }

  public void setUnit(UnitType unit) {
    this.unit = unit;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    DishIngredient that = (DishIngredient) o;
    return Objects.equals(id, that.id)
        && Objects.equals(dish, that.dish)
        && Objects.equals(ingredient, that.ingredient)
        && Objects.equals(quantityRequired, that.quantityRequired)
        && unit == that.unit;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, dish, ingredient, quantityRequired, unit);
  }

  @Override
  public String toString() {
    return "DishIngredient{"
        + "id="
        + id
        + ", dish="
        + dish
        + ", ingredient="
        + ingredient
        + ", quantityRequired="
        + quantityRequired
        + ", unit="
        + unit
        + '}';
  }
}
