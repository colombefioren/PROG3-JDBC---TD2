package com.jdbctd2;

import com.jdbctd2.model.*;
import com.jdbctd2.repository.DataRetriever;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Main {
  public static void main(String[] args) {
    DataRetriever dataRetriever = new DataRetriever();

    dataRetriever.initializeDB();

    System.out.println("\n===> Stock::getQuantity | POO Approach <===");
    List<Ingredient> allIngredients = new ArrayList<>();
    for (int i = 1; i <= 5; i++) {
      try {
        allIngredients.add(dataRetriever.findIngredientById(i));

      } catch (RuntimeException e) {
        System.out.println("Ingredient with id " + i + " not found: " + e.getMessage());
      }
    }

    for (Ingredient ingredient : allIngredients) {
      System.out.println("\nIngredient name : " + ingredient.getName());
      System.out.println(
          "Stock : " + ingredient.getStockValueAt(Instant.parse("2024-01-06T12:00:00Z")));
    }

    System.out.println("\n===> Stock::getQuantity | DB SIDE PROCESSING Approacj <===");

    for (Ingredient ingredient : allIngredients) {
      System.out.println("\nIngredient name : " + ingredient.getName());
      System.out.println(
          "Stock : "
              + dataRetriever.getStockValueAt(
                  Instant.parse("2024-01-06T12:00:00Z"), ingredient.getId()));
    }

    System.out.println("\n===> Test getGrossMargin after db normalization");
    List<Dish> allDishes = new ArrayList<>();
    for (int i = 5; i > 0; i--) {
      try {
        allDishes.add(dataRetriever.findDishById(i));
      } catch (RuntimeException e) {
        System.out.println("Dish with id " + i + " not found: " + e.getMessage());
      }
    }
    for (Dish dish : allDishes) {
      try {
        System.out.println("\nDish name : " + dish.getName());
        System.out.println("Dish price : " + dish.getPrice());
        System.out.println("Dish cost (ingredients) : " + dish.getDishCost());
        System.out.println("Gross Margin : " + dish.getGrossMargin());
      } catch (RuntimeException e) {
        System.out.println(e.getMessage());
      }
    }
  }
}
