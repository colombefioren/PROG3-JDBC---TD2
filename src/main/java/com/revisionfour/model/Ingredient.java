package com.revisionfour.model;

import java.util.Objects;

public class Ingredient {
  private Integer id;
  private String name;
  private CategoryEnum category;
  private Double price;
  private Dish dish;

  public Ingredient() {}

  public Ingredient(Integer id, String name, CategoryEnum category, Double price, Dish dish) {
    this.id = id;
    this.name = name;
    this.category = category;
    this.price = price;
    this.dish = dish;
  }

  public Ingredient(String name, CategoryEnum category, Double price, Dish dish) {
    this.name = name;
    this.category = category;
    this.price = price;
    this.dish = dish;
  }

  public Ingredient(String name, CategoryEnum category, Double price) {
    this.name = name;
    this.category = category;
    this.price = price;
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

  public Dish getDish() {
    return dish;
  }

  public void setDish(Dish dish) {
    this.dish = dish;
  }

  public String getDishName() {
    return this.dish == null ? null : this.dish.getName();
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    Ingredient that = (Ingredient) o;
    return Objects.equals(id, that.id)
        && Objects.equals(name, that.name)
        && category == that.category
        && Objects.equals(dish, that.dish);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, category, dish);
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
        + ", dish="
        + getDishName()
        + '}';
  }
}
